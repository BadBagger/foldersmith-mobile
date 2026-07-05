package com.foldersmith.mobile.domain

import com.foldersmith.mobile.model.FileCategory
import java.util.Locale

object FileCategorizer {
    private const val LARGE_FILE_BYTES = 100L * 1024L * 1024L
    private const val OLD_FILE_AGE_MS = 180L * 24L * 60L * 60L * 1000L

    fun categorize(
        displayName: String,
        mimeType: String?,
        folderLabel: String?,
        sizeBytes: Long,
        dateModified: Long?,
        nowMillis: Long = System.currentTimeMillis()
    ): FileCategory {
        val name = displayName.lowercase(Locale.US)
        val folder = folderLabel.orEmpty().lowercase(Locale.US)
        val type = mimeType.orEmpty().lowercase(Locale.US)
        val extension = name.substringAfterLast('.', missingDelimiterValue = "")

        return when {
            isScreenshot(name, folder) -> FileCategory.Screenshot
            sizeBytes >= LARGE_FILE_BYTES -> FileCategory.LargeFile
            type.startsWith("video/") -> FileCategory.Video
            type.startsWith("image/") && folder.contains("camera") -> FileCategory.CameraPhoto
            type.startsWith("image/") -> FileCategory.Image
            type == "application/pdf" || extension == "pdf" -> FileCategory.Pdf
            extension == "apk" -> FileCategory.Apk
            extension == "zip" || extension == "7z" || extension == "rar" -> FileCategory.Zip
            isDocument(type, extension) -> FileCategory.Document
            folder.contains("download") -> FileCategory.Download
            dateModified != null && nowMillis - dateModified > OLD_FILE_AGE_MS -> FileCategory.OldFile
            else -> FileCategory.Other
        }
    }

    fun isScreenshot(displayName: String, folderLabel: String?): Boolean {
        val name = displayName.lowercase(Locale.US)
        val folder = folderLabel.orEmpty().lowercase(Locale.US)
        return "screenshot" in name ||
            "screen_shot" in name ||
            "screenshots" in folder ||
            "screenshot" in folder
    }

    private fun isDocument(mimeType: String, extension: String): Boolean {
        return mimeType.startsWith("text/") ||
            "document" in mimeType ||
            "msword" in mimeType ||
            extension in setOf("doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv")
    }
}
