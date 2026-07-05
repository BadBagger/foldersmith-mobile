package com.foldersmith.mobile.model

import androidx.room.TypeConverter

enum class FileCategory {
    Screenshot,
    CameraPhoto,
    Download,
    Image,
    Video,
    Pdf,
    Apk,
    Zip,
    Document,
    LargeFile,
    ExactDuplicate,
    PossibleDuplicatePhoto,
    OldFile,
    Other
}

enum class CleanupActionType {
    Keep,
    Move,
    Archive,
    Delete,
    Ignore
}

enum class CleanupStatus {
    Planned,
    Applied,
    Failed,
    Undone
}

enum class ScanType {
    Photos,
    Screenshots,
    Downloads,
    SelectedFolder
}

data class ScanRequest(
    val type: ScanType,
    val selectedTreeUri: String? = null
)

data class DashboardSummary(
    val filesScanned: Int = 0,
    val duplicatesFound: Int = 0,
    val screenshotsFound: Int = 0,
    val downloadsFound: Int = 0,
    val largeFilesFound: Int = 0,
    val suggestedCleanupBytes: Long = 0,
    val lastScanDate: Long? = null
)

data class CleanupPlanSummary(
    val keepCount: Int,
    val archiveCount: Int,
    val moveCount: Int,
    val deleteCount: Int,
    val estimatedBytes: Long,
    val warningCount: Int,
    val skippedCount: Int
)

class EnumConverters {
    @TypeConverter
    fun toFileCategory(value: String?): FileCategory? = value?.let(FileCategory::valueOf)

    @TypeConverter
    fun fromFileCategory(value: FileCategory?): String? = value?.name

    @TypeConverter
    fun toCleanupActionType(value: String?): CleanupActionType? = value?.let(CleanupActionType::valueOf)

    @TypeConverter
    fun fromCleanupActionType(value: CleanupActionType?): String? = value?.name

    @TypeConverter
    fun toCleanupStatus(value: String?): CleanupStatus? = value?.let(CleanupStatus::valueOf)

    @TypeConverter
    fun fromCleanupStatus(value: CleanupStatus?): String? = value?.name

    @TypeConverter
    fun toScanType(value: String?): ScanType? = value?.let(ScanType::valueOf)

    @TypeConverter
    fun fromScanType(value: ScanType?): String? = value?.name
}
