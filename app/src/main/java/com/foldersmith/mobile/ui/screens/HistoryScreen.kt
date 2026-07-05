package com.foldersmith.mobile.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.foldersmith.mobile.model.ScanType
import com.foldersmith.mobile.ui.AppUiState
import com.foldersmith.mobile.ui.formatDateTime

@Composable
fun HistoryScreen(state: AppUiState, onUndo: (Long) -> Unit) {
    ScreenColumn {
        Text("Cleanup history")
        val sessions = state.sessions.filter { it.id > 0L }
        if (sessions.isEmpty()) {
            EmptyState("No cleanup history", "Scans and confirmed cleanup sessions will appear here.")
        } else {
            sessions.forEach { session ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ScreenColumn {
                        Text(session.createdAt.safeHistoryDate())
                        Text("${session.scanType.historyLabel()} - ${session.summary.ifBlank { "Scan saved" }}")
                        Text(if (session.canUndo) "Undo may be possible for moves." else "No applied changes to undo.")
                        OutlinedButton(onClick = { onUndo(session.id) }, enabled = session.canUndo) {
                            Text("Undo moves where possible")
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
