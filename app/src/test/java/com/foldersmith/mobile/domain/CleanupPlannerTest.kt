package com.foldersmith.mobile.domain

import com.foldersmith.mobile.data.DuplicateGroupEntity
import com.foldersmith.mobile.data.ScannedFileEntity
import com.foldersmith.mobile.model.CleanupActionType
import com.foldersmith.mobile.model.FileCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class CleanupPlannerTest {
    @Test
    fun archivesDuplicateCopiesButKeepsRecommendedFile() {
        val files = listOf(file(1, 100), file(2, 100), file(3, 50))
        val groups = listOf(DuplicateGroupEntity(hash = "same", sizeBytes = 100, fileIds = "1,2", recommendedKeepFileId = 1))

        val plan = CleanupPlanner.buildPlan(files, groups, sessionId = 10)

        assertEquals(CleanupActionType.Keep, plan.first { it.fileId == 1L }.actionType)
        assertEquals(CleanupActionType.Archive, plan.first { it.fileId == 2L }.actionType)
        assertEquals(CleanupActionType.Keep, plan.first { it.fileId == 3L }.actionType)
    }

    @Test
    fun summarizesPlannedActionsWithoutCountingKeptFilesAsAffected() {
        val files = listOf(file(1, 100), file(2, 100))
        val groups = listOf(DuplicateGroupEntity(hash = "same", sizeBytes = 100, fileIds = "1,2", recommendedKeepFileId = 1))
        val plan = CleanupPlanner.buildPlan(files, groups, sessionId = 10)

        val summary = CleanupPlanner.summarize(files, plan)

        assertEquals(1, summary.keepCount)
        assertEquals(1, summary.archiveCount)
        assertEquals(100, summary.estimatedBytes)
    }

    private fun file(id: Long, size: Long) = ScannedFileEntity(
        id = id,
        uri = "content://file/$id",
        displayName = "file-$id.jpg",
        mimeType = "image/jpeg",
        sizeBytes = size,
        dateAdded = null,
        dateModified = null,
        dateTaken = null,
        folderLabel = "Pictures",
        hash = "same",
        category = FileCategory.Image,
        scanId = 1
    )
}
