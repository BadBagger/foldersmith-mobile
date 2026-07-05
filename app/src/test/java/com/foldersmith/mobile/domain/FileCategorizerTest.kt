package com.foldersmith.mobile.domain

import com.foldersmith.mobile.model.FileCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FileCategorizerTest {
    @Test
    fun categorizesScreenshotsByNameAndFolder() {
        assertEquals(
            FileCategory.Screenshot,
            FileCategorizer.categorize(
                displayName = "Screenshot_20260705.png",
                mimeType = "image/png",
                folderLabel = "Pictures/Screenshots",
                sizeBytes = 42L,
                dateModified = null
            )
        )
        assertTrue(FileCategorizer.isScreenshot("screen_shot_home.png", "Pictures"))
    }

    @Test
    fun categorizesCommonFileTypes() {
        assertEquals(
            FileCategory.Pdf,
            FileCategorizer.categorize("manual.pdf", "application/pdf", "Download", 2_000L, null)
        )
        assertEquals(
            FileCategory.Apk,
            FileCategorizer.categorize("installer.apk", null, "Files", 2_000L, null)
        )
        assertEquals(
            FileCategory.Zip,
            FileCategorizer.categorize("archive.zip", null, "Files", 2_000L, null)
        )
    }

    @Test
    fun defersToLargeFileBeforeGenericType() {
        assertEquals(
            FileCategory.LargeFile,
            FileCategorizer.categorize("video.mp4", "video/mp4", "Movies", 150L * 1024L * 1024L, null)
        )
    }
}
