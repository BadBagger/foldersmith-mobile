package com.foldersmith.mobile.scanner

import android.content.Context
import android.content.ContentResolver
import android.content.ContentUris
import android.os.Build
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.foldersmith.mobile.data.ScannedFileEntity
import com.foldersmith.mobile.domain.DuplicateDetector
import com.foldersmith.mobile.domain.FileCategorizer
import com.foldersmith.mobile.model.ScanRequest
import com.foldersmith.mobile.model.ScanType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidContentScanner(
    private val context: Context,
    private val contentResolver: ContentResolver,
    private val fileHasher: FileHasher,
    private val maxImmediateHashBytes: Long = 128L * 1024L * 1024L
) : ContentScanner {
    override suspend fun scan(
        request: ScanRequest,
        scanId: Long,
        onProgress: suspend (ScanProgress) -> Unit,
        shouldCancel: () -> Boolean
    ): List<ScannedFileEntity> {
        onProgress(ScanProgress(ScanStep.ReadingFiles, message = "Reading files"))
        val rawFiles = withContext(Dispatchers.IO) {
            when (request.type) {
                ScanType.Photos -> queryImages(scanId, screenshotsOnly = false)
                ScanType.Screenshots -> queryImages(scanId, screenshotsOnly = true)
                ScanType.Downloads -> queryDownloads(scanId)
                ScanType.SelectedFolder -> querySelectedTree(request.selectedTreeUri, scanId)
            }
        }
        if (shouldCancel()) return emptyList()

        onProgress(ScanProgress(ScanStep.GroupingByType, completed = rawFiles.size, total = rawFiles.size, message = "Grouping by type"))
        val candidates = DuplicateDetector.candidateFilesForHashing(rawFiles, maxImmediateHashBytes)

        val hashedByUri = mutableMapOf<String, String?>()
        candidates.forEachIndexed { index, file ->
            if (shouldCancel()) {
                onProgress(ScanProgress(ScanStep.Cancelled, index, candidates.size, "Scan cancelled"))
                return emptyList()
            }
            onProgress(ScanProgress(ScanStep.CheckingDuplicates, index + 1, candidates.size, "Checking duplicates safely"))
            hashedByUri[file.uri] = fileHasher.sha256(Uri.parse(file.uri))
        }

        onProgress(ScanProgress(ScanStep.FindingScreenshots, rawFiles.size, rawFiles.size, "Finding screenshots"))
        val scanned = rawFiles.map { file ->
            file.copy(hash = hashedByUri[file.uri])
        }
        onProgress(ScanProgress(ScanStep.Complete, scanned.size, scanned.size, "Scan complete"))
        return scanned
    }

    private fun queryImages(scanId: Long, screenshotsOnly: Boolean): List<ScannedFileEntity> {
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val folderColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.RELATIVE_PATH
        } else {
            MediaStore.Images.Media.DATA
        }
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DATE_TAKEN,
            folderColumn
        )
        val files = queryMedia(collection, projection, scanId)
        return if (screenshotsOnly) {
            files.filter { FileCategorizer.isScreenshot(it.displayName, it.folderLabel) }
        } else {
            files
        }
    }

    private fun queryDownloads(scanId: Long): List<ScannedFileEntity> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        val folderColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.MediaColumns.RELATIVE_PATH
        } else {
            MediaStore.MediaColumns.DATA
        }
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.DATE_MODIFIED,
            folderColumn
        )
        return queryMedia(collection, projection, scanId)
    }

    private fun queryMedia(collection: Uri, projection: Array<String>, scanId: Long): List<ScannedFileEntity> {
        val files = mutableListOf<ScannedFileEntity>()
        contentResolver.query(collection, projection, null, null, "${projection[0]} DESC")?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(projection[0])
            val nameColumn = cursor.getColumnIndexOrThrow(projection[1])
            val mimeColumn = cursor.getColumnIndex(projection[2])
            val sizeColumn = cursor.getColumnIndex(projection[3])
            val addedColumn = cursor.getColumnIndex(projection[4])
            val modifiedColumn = cursor.getColumnIndex(projection[5])
            val takenColumn = projection.indexOfFirst { it.endsWith("datetaken", ignoreCase = true) }
                .takeIf { it >= 0 }
                ?.let { cursor.getColumnIndex(projection[it]) }
            val folderColumn = cursor.getColumnIndex(projection.last())

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unnamed file"
                val mimeType = mimeColumn.takeIf { it >= 0 }?.let { cursor.getString(it) }
                val size = sizeColumn.takeIf { it >= 0 }?.let { cursor.getLong(it) } ?: 0L
                val dateAdded = addedColumn.takeIf { it >= 0 }?.let { cursor.getLong(it) * 1000L }
                val dateModified = modifiedColumn.takeIf { it >= 0 }?.let { cursor.getLong(it) * 1000L }
                val dateTaken = takenColumn?.takeIf { it >= 0 }?.let { cursor.getLong(it) }
                val folder = folderColumn.takeIf { it >= 0 }?.let { cursor.getString(it) }
                val uri = ContentUris.withAppendedId(collection, id).toString()
                files += ScannedFileEntity(
                    uri = uri,
                    displayName = name,
                    mimeType = mimeType,
                    sizeBytes = size,
                    dateAdded = dateAdded,
                    dateModified = dateModified,
                    dateTaken = dateTaken,
                    folderLabel = folder,
                    hash = null,
                    category = FileCategorizer.categorize(name, mimeType, folder, size, dateModified),
                    scanId = scanId
                )
            }
        }
        return files
    }

    private fun querySelectedTree(selectedTreeUri: String?, scanId: Long): List<ScannedFileEntity> {
        val rootUri = selectedTreeUri?.let(Uri::parse) ?: return emptyList()
        val root = DocumentFile.fromTreeUri(context, rootUri) ?: return emptyList()
        val files = mutableListOf<ScannedFileEntity>()

        fun visit(document: DocumentFile, folderLabel: String, depth: Int) {
            if (depth > 8 || files.size >= 5_000) return
            if (document.isDirectory) {
                document.listFiles().forEach { child ->
                    visit(child, document.name ?: folderLabel, depth + 1)
                }
            } else if (document.isFile) {
                val name = document.name ?: "Unnamed file"
                val mimeType = document.type
                val size = document.length().takeIf { it >= 0L } ?: 0L
                val modified = document.lastModified().takeIf { it > 0L }
                files += ScannedFileEntity(
                    uri = document.uri.toString(),
                    displayName = name,
                    mimeType = mimeType,
                    sizeBytes = size,
                    dateAdded = null,
                    dateModified = modified,
                    dateTaken = null,
                    folderLabel = folderLabel,
                    hash = null,
                    category = FileCategorizer.categorize(name, mimeType, folderLabel, size, modified),
                    scanId = scanId
                )
            }
        }

        visit(root, root.name ?: "Selected folder", 0)
        return files
    }
}
