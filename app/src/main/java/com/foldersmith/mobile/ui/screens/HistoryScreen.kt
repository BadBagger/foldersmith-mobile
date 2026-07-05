package com.foldersmith.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foldersmith.mobile.model.ScanType
import com.foldersmith.mobile.ui.AppUiState
import com.foldersmith.mobile.ui.formatDateTime

@Composable
fun HistoryScreen(state: AppUiState, onUndo: (Long) -> Unit) {
    ScreenColumn {
        Text("History", style = MaterialTheme.typography.headlineSmall)
        Text("Scans and applied cleanup sessions. Undo only appears when Android permissions make it possible.")

        val sessions = state.sessions.filter { it.id > 0L }
        if (sessions.isEmpty()) {
            EmptyState("No cleanup history", "Scans and confirmed cleanup sessions will appear here.")
        } else {
            sessions.forEach { session ->
                SectionCard(title = session.createdAt.safeHistoryDate()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("${session.scanType.historyLabel()} - ${session.summary.ifBlank { "Scan saved" }}")
                        Text(if (session.canUndo) "Undo may be possible for moves." else "No applied changes to undo.")
                        if (session.canUndo) {
                            Button(onClick = { onUndo(session.id) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Undo moves where possible")
                            }
                        } else {
                            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                                Text("Nothing to undo")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Long.safeHistoryDate(): String = runCatching {
    formatDateTime()
}.getOrDefault("Saved cleanup")

private fun ScanType.historyLabel(): String = when (this) {
    ScanType.Photos -> "Photos scan"
    ScanType.Screenshots -> "Screenshots scan"
    ScanType.Downloads -> "Downloads scan"
    ScanType.SelectedFolder -> "Selected folder scan"
}
