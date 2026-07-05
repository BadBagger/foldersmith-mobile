package com.foldersmith.mobile.domain

import com.foldersmith.mobile.data.ScannedFileEntity
import com.foldersmith.mobile.model.FileCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicateDetectorTest {
    @Test
    fun groupsExactDuplicatesBySizeAndHash() {
        val files = listOf(
            file(1, 100, "hash-a"),
            file(2, 100, "hash-a"),
            file(3, 100, "hash-b"),
            file(4, 200, "hash-a")
        )

        val groups = DuplicateDetector.exactDuplicateGroups(files)

        assertEquals(1, groups.size)
        assertEquals("1,2", groups.first().fileIds)
        assertEquals(100, groups.first().sizeBytes)
    }

    @Test
    fun hashesOnlySameSizeCandidatesBelowLimit() {
        val files = listOf(
            file(1, 100, null),
            file(2, 100, null),
            file(3, 200, null),
            file(4, 300, null),
            file(5, 300, null)
        )

        val candidates = DuplicateDetector.candidateFilesForHashing(files, maxImmediateHashBytes = 200)

        assertEquals(listOf(1L, 2L), candidates.map { it.id })
        assertTrue(candidates.none { it.id == 5L })
    }

    private fun file(id: Long, size: Long, hash: String?) = ScannedFileEntity(
        id = id,
        uri = "content://file/$id",
        displayName = "file-$id.jpg",
        mimeType = "image/jpeg",
        sizeBytes = size,
        dateAdded = null,
        dateModified = null,
        dateTaken = null,
        folderLabel = "Pictures",
        hash = hash,
        category = FileCategory.Image,
        scanId = 1
    )
}
