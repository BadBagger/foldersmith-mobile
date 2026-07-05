package com.foldersmith.mobile.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.foldersmith.mobile.ui.AppUiState

@Composable
fun DownloadsScreen(state: AppUiState) {
    ScreenColumn {
        Text("Downloads")
        if (state.downloads.isEmpty()) {
            EmptyState("No downloads found", "Scan downloads to group large files, APKs, ZIPs, PDFs, images, videos, and unknown files.")
        } else {
            state.downloads.forEach { FileRow(it) }
        }
    }
}
