package com.foldersmith.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.foldersmith.mobile.model.CleanupActionType
import com.foldersmith.mobile.model.CleanupStatus
import com.foldersmith.mobile.model.FileCategory
import com.foldersmith.mobile.model.ScanType

@Entity(tableName = "scanned_files")
data class ScannedFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val displayName: String,
    val mimeType: String?,
    val sizeBytes: Long,
    val dateAdded: Long?,
    val dateModified: Long?,
    val dateTaken: Long?,
    val folderLabel: String?,
    val hash: String?,
    val category: FileCategory,
    val scanId: Long,
    val accessDenied: Boolean = false
)

@Entity(tableName = "duplicate_groups")
data class DuplicateGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hash: String,
    val sizeBytes: Long,
    val fileIds: String,
    val recommendedKeepFileId: Long?
)

@Entity(tableName = "cleanup_actions")
data class CleanupActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,
    val actionType: CleanupActionType,
    val sourceUri: String,
    val destinationUri: String?,
    val status: CleanupStatus,
    val sessionId: Long
)

@Entity(tableName = "cleanup_sessions")
data class CleanupSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val scanType: ScanType,
    val summary: String,
    val canUndo: Boolean
)

@Entity(tableName = "photo_events")
data class PhotoEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startDate: Long,
    val endDate: Long,
    val fileIds: String,
    val userEditedTitle: Boolean
)
