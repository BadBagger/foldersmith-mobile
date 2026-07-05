package com.foldersmith.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.foldersmith.mobile.model.CleanupActionType
import com.foldersmith.mobile.ui.AppUiState
import com.foldersmith.mobile.ui.formatBytes

@Composable
fun OrganizedFolderScreen(state: AppUiState) {
    val archiveActions = state.cleanupActions.filter {
        it.actionType == CleanupActionType.Archive || it.actionType == CleanupActionType.Move
    }
    ScreenColumn {
        Text("FolderSmith Organized", style = MaterialTheme.typography.headlineSmall)
        Text("Suggested folders for files you approve. Nothing is applied from scan alone.")
        if (archiveActions.isEmpty()) {
            EmptyState("No destinations yet", "Run a scan and review the plan to create organized folder suggestions.")
        } else {
            val filesById = state.files.associateBy { it.id }
            val grouped = archiveActions.groupBy { it.destinationUri ?: "FolderSmith Organized/Needs Review" }
            grouped.forEach { (destination, actions) ->
                val files = actions.mapNotNull { filesById[it.fileId] }
                SectionCard(title = destination) {
                    Text("${files.size} files - ${files.sumOf { it.sizeBytes }.formatBytes()}")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        files.take(3).forEach { file ->
                            Text(file.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (files.size > 3) {
                        Text("+${files.size - 3} more", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
