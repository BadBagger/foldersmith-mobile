package com.foldersmith.mobile.data

import com.foldersmith.mobile.domain.CleanupPlanner
import com.foldersmith.mobile.domain.DuplicateDetector
import com.foldersmith.mobile.domain.PhotoEventGrouper
import com.foldersmith.mobile.domain.UndoSessionGenerator
import com.foldersmith.mobile.model.CleanupStatus
import com.foldersmith.mobile.model.DashboardSummary
import com.foldersmith.mobile.model.FileCategory
import com.foldersmith.mobile.model.ScanRequest
import com.foldersmith.mobile.model.ScanType
import com.foldersmith.mobile.scanner.ContentScanner
import com.foldersmith.mobile.scanner.ScanProgress
import com.foldersmith.mobile.scanner.ScanStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FolderSmithRepository(
    private val database: FolderSmithDatabase,
    private val scanner: ContentScanner
) {
    private val dao = database.dao()

    val files: Flow<List<ScannedFileEntity>> = dao.observeFiles()
    val duplicateGroups: Flow<List<DuplicateGroupEntity>> = dao.observeDuplicateGroups()
    val cleanupActions: Flow<List<CleanupActionEntity>> = dao.observeCleanupActions()
    val sessions: Flow<List<CleanupSessionEntity>> = dao.observeCleanupSessions()
    val photoEvents: Flow<List<PhotoEventEntity>> = dao.observePhotoEvents()

    val dashboardSummary: Flow<DashboardSummary> = combine(files, duplicateGroups, sessions) { files, groups, sessions ->
        DashboardSummary(
            filesScanned = files.size,
            duplicatesFound = groups.sumOf { it.fileIds.split(',').size },
            screenshotsFound = files.count { it.category == FileCategory.Screenshot },
            downloadsFound = files.count {
                it.category == FileCategory.Download || it.folderLabel.orEmpty().contains("download", ignoreCase = true)
            },
            largeFilesFound = files.count { it.category == FileCategory.LargeFile },
            suggestedCleanupBytes = groups.sumOf { group ->
                val duplicateCount = group.fileIds.split(',').size
                if (duplicateCount > 1) group.sizeBytes * (duplicateCount - 1) else 0L
            },
            lastScanDate = sessions.firstOrNull()?.createdAt
        )
    }

    fun screenshots(): Flow<List<ScannedFileEntity>> = dao.observeFilesByCategory(FileCategory.Screenshot)

    fun downloads(): Flow<List<ScannedFileEntity>> = files.map { all ->
        all.filter { it.category == FileCategory.Download || it.folderLabel.orEmpty().contains("download", ignoreCase = true) }
    }

    suspend fun runScan(
        request: ScanRequest,
        onProgress: suspend (ScanProgress) -> Unit,
        shouldCancel: () -> Boolean
    ) {
        val sessionId = dao.insertSession(
            CleanupSessionEntity(
                createdAt = System.currentTimeMillis(),
                scanType = request.type,
                summary = "Scan started",
                canUndo = false
            )
        )
        dao.clearScannedFiles()
        dao.clearDuplicateGroups()
        dao.clearPhotoEvents()

        val scanned = scanner.scan(request, sessionId, onProgress, shouldCancel)
        if (shouldCancel()) {
            onProgress(ScanProgress(ScanStep.Cancelled, message = "Scan cancelled"))
            return
        }
        val generatedIds = dao.insertFiles(scanned)
        val persistedFiles = scanned.zip(generatedIds) { file, id -> file.copy(id = id) }
        val groups = DuplicateDetector.exactDuplicateGroups(persistedFiles)
        dao.insertDuplicateGroups(groups)
        dao.insertPhotoEvents(PhotoEventGrouper.group(persistedFiles))
        val actions = CleanupPlanner.buildPlan(persistedFiles, groups, sessionId)
        dao.insertCleanupActions(actions)
        onProgress(ScanProgress(ScanStep.Complete, persistedFiles.size, persistedFiles.size, "Results are ready to review"))
    }

    suspend fun markSessionUndone(sessionId: Long) {
        val actions = dao.cleanupActionsForSession(sessionId)
        val undoable = UndoSessionGenerator.undoableActions(actions)
        if (undoable.isNotEmpty()) {
            dao.updateSessionActionStatus(sessionId, CleanupStatus.Applied, CleanupStatus.Undone)
        }
    }

    fun scanTypes(): List<ScanType> = ScanType.entries
}
