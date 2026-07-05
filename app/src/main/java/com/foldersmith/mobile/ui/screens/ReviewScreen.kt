package com.foldersmith.mobile.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foldersmith.mobile.data.CleanupActionEntity
import com.foldersmith.mobile.data.ScannedFileEntity
import com.foldersmith.mobile.model.CleanupActionType
import com.foldersmith.mobile.model.CleanupStatus
import com.foldersmith.mobile.ui.AppUiState
import com.foldersmith.mobile.ui.formatBytes

@Composable
fun ReviewScreen(
    state: AppUiState,
    onOpenOrganizedFolder: () -> Unit,
    onApplyCleanup: (String) -> Unit
) {
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            onApplyCleanup(uri.toString())
        }
    }
    val actionableCount = state.cleanupActions.count {
        it.status == CleanupStatus.Planned && it.actionType in setOf(CleanupActionType.Move, CleanupActionType.Archive)
    }

    ScreenColumn {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Review plan", style = MaterialTheme.typography.headlineSmall)
            Text("Nothing changes until you approve a plan.")
        }

        if (state.cleanupActions.isEmpty()) {
            EmptyState("No plan yet", "Run a safe scan first. FolderSmith will show destinations before any move, archive, or delete action.")
            return@ScreenColumn
        }

        val summary = state.planSummary
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Move", summary.moveCount.toString(), Modifier.weight(1f))
            MetricCard("Archive", summary.archiveCount.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Keep", summary.keepCount.toString(), Modifier.weight(1f))
            MetricCard("Delete", summary.deleteCount.toString(), Modifier.weight(1f))
        }

        SectionCard(
            title = "Plan goal",
            body = "Create a FolderSmith Organized area with reviewable folders. Exact duplicate copies are archived. Screenshots and downloads get safe suggested paths. Delete stays at zero."
        ) {
            StatusPill("Estimated affected ${summary.estimatedBytes.formatBytes()}")
            StatusPill("${summary.skippedCount} skipped")
        }

        PlanLane(
            title = "Archive exact duplicate copies",
            actionType = CleanupActionType.Archive,
            state = state,
            emptyText = "No exact duplicate copies selected for archive."
        )
        PlanLane(
            title = "Move into organized folders",
            actionType = CleanupActionType.Move,
            state = state,
            emptyText = "No screenshots, downloads, or documents need organization from this scan."
        )
        SectionCard(
            title = "Next step",
            body = "Choose where FolderSmith should create its organized folder. This copies files first and leaves originals untouched."
        ) {
            if (state.organizeProgress.isApplying) {
                LinearProgressIndicator(
                    progress = {
                        if (state.organizeProgress.total == 0) 0f
                        else state.organizeProgress.completed.toFloat() / state.organizeProgress.total.toFloat()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(state.organizeProgress.message, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onOpenOrganizedFolder, modifier = Modifier.fillMaxWidth()) {
                Text("Review organized destinations")
            }
            OutlinedButton(
                onClick = { folderPicker.launch(null) },
                enabled = actionableCount > 0 && !state.organizeProgress.isApplying,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Choose folder and copy $actionableCount files")
            }
        }
    }
}

@Composable
private fun PlanLane(
    title: String,
    actionType: CleanupActionType,
    state: AppUiState,
    emptyText: String
) {
    val actions = state.cleanupActions.filter { it.actionType == actionType }
    val filesById = state.files.associateBy { it.id }
    val destinations = actions
        .mapNotNull { it.destinationUri }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }

    SectionCard(title = title) {
        if (actions.isEmpty()) {
            Text(emptyText, style = MaterialTheme.typography.bodyMedium)
        } else {
            Text("${actions.size} files", style = MaterialTheme.typography.bodyMedium)
            destinations.take(4).forEach { destination ->
                StatusPill("${destination.key} (${destination.value})")
            }
            actions.take(3).forEach { action ->
                val file = filesById[action.fileId]
                if (file != null) {
                    CompactPlanRow(file, action)
                }
            }
            if (actions.size > 3) {
                Text("+${actions.size - 3} more hidden for readability", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CompactPlanRow(file: ScannedFileEntity, action: CleanupActionEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(file.displayName, style = MaterialTheme.typography.bodyMedium)
        Text(
            "${file.sizeBytes.formatBytes()} -> ${action.destinationUri ?: "No move planned"}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
