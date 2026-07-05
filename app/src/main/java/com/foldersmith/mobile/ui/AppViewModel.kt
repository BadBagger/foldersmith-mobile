package com.foldersmith.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.foldersmith.mobile.data.CleanupActionEntity
import com.foldersmith.mobile.data.CleanupSessionEntity
import com.foldersmith.mobile.data.DuplicateGroupEntity
import com.foldersmith.mobile.data.FolderSmithRepository
import com.foldersmith.mobile.data.PhotoEventEntity
import com.foldersmith.mobile.data.ScannedFileEntity
import com.foldersmith.mobile.domain.CleanupPlanner
import com.foldersmith.mobile.model.CleanupPlanSummary
import com.foldersmith.mobile.model.DashboardSummary
import com.foldersmith.mobile.model.ScanRequest
import com.foldersmith.mobile.model.ScanType
import com.foldersmith.mobile.organize.OrganizeProgress
import com.foldersmith.mobile.scanner.ScanProgress
import com.foldersmith.mobile.scanner.ScanStep
import com.foldersmith.mobile.subscription.BillingGateway
import com.foldersmith.mobile.subscription.BillingResult
import com.foldersmith.mobile.subscription.FridgeSubscriptionState
import com.foldersmith.mobile.subscription.PlaceholderBillingGateway
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUiState(
    val dashboard: DashboardSummary = DashboardSummary(),
    val files: List<ScannedFileEntity> = emptyList(),
    val duplicateGroups: List<DuplicateGroupEntity> = emptyList(),
    val cleanupActions: List<CleanupActionEntity> = emptyList(),
    val sessions: List<CleanupSessionEntity> = emptyList(),
    val screenshots: List<ScannedFileEntity> = emptyList(),
    val downloads: List<ScannedFileEntity> = emptyList(),
    val photoEvents: List<PhotoEventEntity> = emptyList(),
    val scanProgress: ScanProgress = ScanProgress(),
    val organizeProgress: OrganizeProgress = OrganizeProgress(),
    val fridgeSubscription: FridgeSubscriptionState = FridgeSubscriptionState(),
    val isScanning: Boolean = false
) {
    val planSummary: CleanupPlanSummary
        get() = CleanupPlanner.summarize(files, cleanupActions)
}

private data class CoreUiState(
    val dashboard: DashboardSummary,
    val files: List<ScannedFileEntity>,
    val duplicateGroups: List<DuplicateGroupEntity>,
    val cleanupActions: List<CleanupActionEntity>,
    val sessions: List<CleanupSessionEntity>
)

private data class LibraryUiState(
    val screenshots: List<ScannedFileEntity>,
    val downloads: List<ScannedFileEntity>,
    val photoEvents: List<PhotoEventEntity>
)

private data class ProgressUiState(
    val scanProgress: ScanProgress,
    val organizeProgress: OrganizeProgress,
    val fridgeSubscription: FridgeSubscriptionState,
    val isScanning: Boolean
)

class AppViewModel(
    private val repository: FolderSmithRepository,
    private val billingGateway: BillingGateway = PlaceholderBillingGateway()
) : ViewModel() {
    private val scanProgress = MutableStateFlow(ScanProgress())
    private val organizeProgress = MutableStateFlow(OrganizeProgress())
    private val fridgeItemCount = MutableStateFlow(0)
    private val billingMessage = MutableStateFlow<String?>(null)
    private val isScanning = MutableStateFlow(false)
    private var scanJob: Job? = null

    private val fridgeSubscription = combine(
        billingGateway.subscriptionTier,
        fridgeItemCount,
        billingMessage
    ) { tier, count, message ->
        FridgeSubscriptionState(tier = tier, fridgeItemCount = count, billingMessage = message)
    }

    private val coreState = combine(
        repository.dashboardSummary,
        repository.files,
        repository.duplicateGroups,
        repository.cleanupActions,
        repository.sessions
    ) { dashboard, files, groups, actions, sessions ->
        CoreUiState(dashboard, files, groups, actions, sessions)
    }

    private val libraryState = combine(
        repository.screenshots(),
        repository.downloads(),
        repository.photoEvents
    ) { screenshots, downloads, events ->
        LibraryUiState(screenshots, downloads, events)
    }

    private val progressState = combine(
        scanProgress,
        organizeProgress,
        fridgeSubscription,
        isScanning
    ) { scanProgress, organizeProgress, subscription, scanning ->
        ProgressUiState(scanProgress, organizeProgress, subscription, scanning)
    }

    val uiState: StateFlow<AppUiState> = combine(
        coreState,
        libraryState,
        progressState
    ) { core, library, progress ->
        AppUiState(
            dashboard = core.dashboard,
            files = core.files,
            duplicateGroups = core.duplicateGroups,
            cleanupActions = core.cleanupActions,
            sessions = core.sessions,
            screenshots = library.screenshots,
            downloads = library.downloads,
            photoEvents = library.photoEvents,
            scanProgress = progress.scanProgress,
            organizeProgress = progress.organizeProgress,
            fridgeSubscription = progress.fridgeSubscription,
            isScanning = progress.isScanning
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    fun startScan(type: ScanType, selectedTreeUri: String? = null) {
        if (scanJob?.isActive == true) return
        scanJob = viewModelScope.launch {
            isScanning.value = true
            scanProgress.value = ScanProgress(ScanStep.ReadingFiles, message = "Preparing safe scan")
            try {
                repository.runScan(
                    request = ScanRequest(type, selectedTreeUri),
                    onProgress = { scanProgress.value = it },
                    shouldCancel = { scanJob?.isCancelled == true }
                )
            } catch (exception: SecurityException) {
                scanProgress.value = ScanProgress(ScanStep.Failed, message = "Permission is needed before scanning")
            } catch (exception: Exception) {
                scanProgress.value = ScanProgress(ScanStep.Failed, message = exception.message ?: "Scan failed")
            } finally {
                isScanning.value = false
            }
        }
    }

    fun cancelScan() {
        scanJob?.cancel()
        scanProgress.value = ScanProgress(ScanStep.Cancelled, message = "Scan cancelled")
        isScanning.value = false
    }

    fun undoSession(sessionId: Long) {
        viewModelScope.launch {
            repository.markSessionUndone(sessionId)
        }
    }

    fun applyCleanupPlan(destinationTreeUri: String) {
        if (organizeProgress.value.isApplying) return
        viewModelScope.launch {
            organizeProgress.value = OrganizeProgress(isApplying = true, message = "Preparing organized folder")
            try {
                repository.applyCleanupPlan(destinationTreeUri) { progress ->
                    organizeProgress.value = progress
                }
            } catch (exception: SecurityException) {
                organizeProgress.value = OrganizeProgress(message = "Folder permission was denied")
            } catch (exception: Exception) {
                organizeProgress.value = OrganizeProgress(message = exception.message ?: "Organizing failed")
            }
        }
    }

    fun startPlusPurchase() {
        viewModelScope.launch {
            billingMessage.value = billingGateway.startPlusPurchase().toUserMessage()
        }
    }

    fun restorePlusPurchases() {
        viewModelScope.launch {
            billingMessage.value = billingGateway.restorePurchases().toUserMessage()
        }
    }

    private fun BillingResult.toUserMessage(): String {
        return when (this) {
            BillingResult.Unavailable -> "Google Play Billing is not connected yet. No purchase was started."
            BillingResult.Pending -> "Purchase is pending."
            is BillingResult.Failed -> reason
        }
    }
}

class AppViewModelFactory(private val repository: FolderSmithRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppViewModel(repository) as T
    }
}
