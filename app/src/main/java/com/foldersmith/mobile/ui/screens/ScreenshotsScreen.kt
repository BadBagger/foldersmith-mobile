package com.foldersmith.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.foldersmith.mobile.ui.AppUiState

@Composable
fun ScreenshotsScreen(state: AppUiState) {
    ScreenColumn {
        Text("Screenshots")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Receipts", "Schedules", "Orders", "Notes", "Memes/Junk", "Codes", "Other").forEach {
                AssistChip(onClick = {}, label = { Text(it) })
            }
        }
        if (state.screenshots.isEmpty()) {
            EmptyState("No screenshots found", "Scan screenshots to review old captures, receipts, codes, and notes safely.")
        } else {
            state.screenshots.forEach { FileRow(it) }
        }
    }
}
