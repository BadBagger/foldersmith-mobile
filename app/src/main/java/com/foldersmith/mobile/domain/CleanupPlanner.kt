package com.foldersmith.mobile.domain

import com.foldersmith.mobile.data.CleanupActionEntity
import com.foldersmith.mobile.data.DuplicateGroupEntity
import com.foldersmith.mobile.data.ScannedFileEntity
import com.foldersmith.mobile.model.CleanupActionType
import com.foldersmith.mobile.model.CleanupPlanSummary
import com.foldersmith.mobile.model.CleanupStatus
import com.foldersmith.mobile.model.FileCategory

object CleanupPlanner {
    fun buildPlan(
        files: List<ScannedFileEntity>,
        duplicateGroups: List<DuplicateGroupEntity>,
        sessionId: Long
    ): List<CleanupActionEntity> {
        val duplicateArchiveIds = duplicateGroups.flatMap { group ->
            val ids = group.fileIds.split(',').mapNotNull { it.toLongOrNull() }
            ids.filterNot { it == group.recommendedKeepFileId }
        }.toSet()

        return files.map { file ->
            val actionType = when {
                file.accessDenied -> CleanupActionType.Ignore
                file.id in duplicateArchiveIds -> CleanupActionType.Archive
                file.category in setOf(FileCategory.Screenshot, FileCategory.Download, FileCategory.Apk, FileCategory.Zip, FileCategory.Pdf, FileCategory.Document) -> CleanupActionType.Move
                else -> CleanupActionType.Keep
            }
            val destination = when {
                file.id in duplicateArchiveIds -> "FolderSmith Organized/Duplicates"
                file.category == FileCategory.Screenshot -> "FolderSmith Organized/Screenshots/Review"
                file.category == FileCategory.Download -> "FolderSmith Organized/Downloads/General"
                file.category == FileCategory.Apk -> "FolderSmith Organized/Downloads/APK Installers"
                file.category == FileCategory.Zip -> "FolderSmith Organized/Downloads/Archives"
                file.category == FileCategory.Pdf -> "FolderSmith Organized/Documents/PDFs"
                file.category == FileCategory.Document -> "FolderSmith Organized/Documents"
                else -> null
            }
            CleanupActionEntity(
                fileId = file.id,
                actionType = actionType,
                sourceUri = file.uri,
                destinationUri = destination,
                status = CleanupStatus.Planned,
                sessionId = sessionId
            )
        }
    }

    fun summarize(files: List<ScannedFileEntity>, actions: List<CleanupActionEntity>): CleanupPlanSummary {
        val byId = files.associateBy { it.id }
        val affectedBytes = actions
            .filter { it.actionType in setOf(CleanupActionType.Archive, CleanupActionType.Move, CleanupActionType.Delete) }
            .sumOf { byId[it.fileId]?.sizeBytes ?: 0L }
        return CleanupPlanSummary(
            keepCount = actions.count { it.actionType == CleanupActionType.Keep },
            archiveCount = actions.count { it.actionType == CleanupActionType.Archive },
            moveCount = actions.count { it.actionType == CleanupActionType.Move },
            deleteCount = actions.count { it.actionType == CleanupActionType.Delete },
            estimatedBytes = affectedBytes,
            warningCount = actions.count { it.actionType == CleanupActionType.Delete },
            skippedCount = actions.count { it.actionType == CleanupActionType.Ignore }
        )
    }
}
