package com.foldersmith.mobile.domain

import com.foldersmith.mobile.data.PhotoEventEntity
import com.foldersmith.mobile.data.ScannedFileEntity
import com.foldersmith.mobile.model.FileCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoEventGrouper {
    private const val EVENT_GAP_MS = 6L * 60L * 60L * 1000L

    fun group(files: List<ScannedFileEntity>): List<PhotoEventEntity> {
        val photos = files
            .filter { it.category in setOf(FileCategory.CameraPhoto, FileCategory.Image, FileCategory.Screenshot) }
            .mapNotNull { file -> (file.dateTaken ?: file.dateModified)?.let { it to file } }
            .sortedBy { it.first }

        if (photos.isEmpty()) return emptyList()

        val groups = mutableListOf<MutableList<Pair<Long, ScannedFileEntity>>>()
        photos.forEach { item ->
            val current = groups.lastOrNull()
            if (current == null || item.first - current.last().first > EVENT_GAP_MS) {
                groups += mutableListOf(item)
            } else {
                current += item
            }
        }

        val formatter = SimpleDateFormat("MMM d", Locale.US)
        return groups.map { group ->
            val start = group.first().first
            val end = group.last().first
            val label = if (group.any { it.second.category == FileCategory.Screenshot }) {
                "Work Screenshots - ${formatter.format(Date(start))}"
            } else {
                "Photo Event - ${formatter.format(Date(start))}"
            }
            PhotoEventEntity(
                title = label,
                startDate = start,
                endDate = end,
                fileIds = group.joinToString(",") { it.second.id.toString() },
                userEditedTitle = false
            )
        }
    }
}
