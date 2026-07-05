package com.foldersmith.mobile.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.foldersmith.mobile.ui.AppUiState
import com.foldersmith.mobile.ui.formatBytes

@Composable
fun DuplicatesScreen(state: AppUiState) {
    ScreenColumn {
        Text("Exact duplicates")
        if (state.duplicateGroups.isEmpty()) {
            EmptyState("No exact duplicates found", "FolderSmith checks matching file sizes first, then hashes matching candidates.")
        } else {
            state.duplicateGroups.forEach { group ->
                val ids = group.fileIds.split(',').mapNotNull { it.toLongOrNull() }
                Text("${ids.size} copies • ${group.sizeBytes.formatBytes()}")
                state.files.filter { it.id in ids }.forEach { file ->
                    FileRow(file)
                }
            }
        }
    }
}
