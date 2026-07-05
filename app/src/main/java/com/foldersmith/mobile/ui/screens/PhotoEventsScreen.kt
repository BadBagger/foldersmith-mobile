package com.foldersmith.mobile.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.foldersmith.mobile.ui.AppUiState
import com.foldersmith.mobile.ui.formatDateTime

@Composable
fun PhotoEventsScreen(state: AppUiState) {
    ScreenColumn {
        Text("Photo Events")
        Text("Photos are grouped by time proximity, date taken, and available location metadata. Face recognition is not part of version 1.")
        if (state.photoEvents.isEmpty()) {
            EmptyState("No events yet", "Scan photos to create editable event groups such as Park Day, Birthday Weekend, or Receipts.")
        } else {
            state.photoEvents.forEach { event ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ScreenColumn {
                        Text(event.title)
                        Text("${event.startDate.formatDateTime()} to ${event.endDate.formatDateTime()}")
                        Text("${event.fileIds.split(',').size} files")
                    }
                }
            }
        }
    }
}
