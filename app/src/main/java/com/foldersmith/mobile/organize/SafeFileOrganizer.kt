package com.foldersmith.mobile.organize

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.foldersmith.mobile.data.CleanupActionEntity
import com.foldersmith.mobile.data.ScannedFileEntity
import com.foldersmith.mobile.domain.FilenameConflictResolver
import com.foldersmith.mobile.model.CleanupActionType
import com.foldersmith.mobile.model.CleanupStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class OrganizeResult(
    val updatedActions: List<CleanupActionEntity>,
    val copiedCount: Int,
    val failedCount: Int
)

class SafeFileOrganizer(
    private val context: Context,
    private val contentResolver: ContentResolver
) {
    suspend fun applyPlan(
        destinationTreeUri: String,
        files: List<ScannedFileEntity>,
        actions: List<CleanupActionEntity>,
        onProgress: suspend (OrganizeProgress) -> Unit
    ): OrganizeResult = withContext(Dispatchers.IO) {
        persistTreePermission(destinationTreeUri)
        val root = DocumentFile.fromTreeUri(context, Uri.parse(destinationTreeUri))
            ?: return@withContext OrganizeResult(emptyList(), copiedCount = 0, failedCount = actions.size)
        val filesById = files.associateBy { it.id }
        val applyActions = actions.filter {
            it.status == CleanupStatus.Planned &&
                it.actionType in setOf(CleanupActionType.Move, CleanupActionType.Archive)
        }
        val updated = mutableListOf<CleanupActionEntity>()
        var copied = 0
        var failed = 0

        applyActions.forEachIndexed { index, action ->
            onProgress(
                OrganizeProgress(
                    isApplying = true,
                    completed = index,
                    total = applyActions.size,
                    message = "Copying ${index + 1} of ${applyActions.size}",
                    failed = failed
                )
            )
            val file = filesById[action.fileId]
            val copiedOk = if (file == null) {
                false
            } else {
                copyFile(root, file, action.destinationUri)
            }
            if (copiedOk) {
                copied += 1
                updated += action.copy(status = CleanupStatus.Applied)
            } else {
                failed += 1
                updated += action.copy(status = CleanupStatus.Failed)
            }
        }

        onProgress(
            OrganizeProgress(
                isApplying = false,
                completed = applyActions.size,
                total = applyActions.size,
                message = "Copied $copied files. $failed failed.",
                failed = failed
            )
        )
        OrganizeResult(updated, copied, failed)
    }

    private fun persistTreePermission(destinationTreeUri: String) {
        runCatching {
            contentResolver.takePersistableUriPermission(
                Uri.parse(destinationTreeUri),
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
    }

    private fun copyFile(root: DocumentFile, file: ScannedFileEntity, plannedDestination: String?): Boolean {
        val destinationDir = ensureDirectory(root, plannedDestination.orEmpty())
            ?: return false
        val existingNames = destinationDir.listFiles().mapNotNull { it.name }.toSet()
        val safeName = FilenameConflictResolver.safeName(file.displayName, existingNames)
        val target = destinationDir.createFile(file.mimeType ?: "application/octet-stream", safeName)
            ?: return false

        return runCatching {
            contentResolver.openInputStream(Uri.parse(file.uri)).use { input ->
                contentResolver.openOutputStream(target.uri, "w").use { output ->
                    if (input == null || output == null) return false
                    input.copyTo(output)
                }
            }
            true
        }.getOrDefault(false)
    }

    private fun ensureDirectory(root: DocumentFile, plannedDestination: String): DocumentFile? {
        val segments = plannedDestination
            .split('/')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("FolderSmith Organized", "Needs Review") }

        var current = root
        segments.forEach { segment ->
            current = current.findFile(segment)?.takeIf { it.isDirectory }
                ?: current.createDirectory(segment)
                ?: return null
        }
        return current
    }
}
