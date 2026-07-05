package com.foldersmith.mobile.scanner

import com.foldersmith.mobile.model.ScanRequest

data class ScanProgress(
    val step: ScanStep = ScanStep.Idle,
    val completed: Int = 0,
    val total: Int = 0,
    val message: String = "Ready"
)

enum class ScanStep {
    Idle,
    ReadingFiles,
    GroupingByType,
    CheckingDuplicates,
    FindingScreenshots,
    BuildingPlan,
    Complete,
    Cancelled,
    Failed
}

interface ContentScanner {
    suspend fun scan(
        request: ScanRequest,
        scanId: Long,
        onProgress: suspend (ScanProgress) -> Unit,
        shouldCancel: () -> Boolean
    ): List<com.foldersmith.mobile.data.ScannedFileEntity>
}
