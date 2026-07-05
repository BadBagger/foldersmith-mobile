package com.foldersmith.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foldersmith.mobile.model.CleanupActionType
import com.foldersmith.mobile.ui.AppUiState
import com.foldersmith.mobile.ui.formatBytes

@Composable
fun ReviewScreen(state: AppUiState) {
    ScreenColumn {
        Text("Cleanup plan")
        if (state.cleanupActions.isEmpty()) {
            EmptyState("No plan yet", "Run a safe scan to build a preview before anything changes.")
        } else {
            val summary = state.planSummary
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AssistChip(onClick = {}, label = { Text("Keep ${summary.keepCount}") })
                AssistChip(onClick = {}, label = { Text("Archive ${summary.archiveCount}") })
                AssistChip(onClick = {}, label = { Text("Move ${summary.moveCount}") })
                AssistChip(onClick = {}, label = { Text("Delete ${summary.deleteCount}") })
            }
            Text("Estimated space affected: ${summary.estimatedBytes.formatBytes()}")
            if (summary.warningCount > 0) {
                Text("Deletion requires explicit confirmation and is never selected automatically.")
            }
            if (summary.skippedCount > 0) {
                Text("${summary.skippedCount} files were skipped because Android denied access.")
            }
            state.cleanupActions.take(60).forEach { action ->
                val file = state.files.firstOrNull { it.id == action.fileId }
                Card(modifier = Modifier.fillMaxWidth()) {
                    ScreenColumn {
                        Text(action.actionType.name)
                        Text(file?.displayName ?: action.sourceUri)
                        Text(action.status.name)
                    }
                }
            }
        }
    }
}
