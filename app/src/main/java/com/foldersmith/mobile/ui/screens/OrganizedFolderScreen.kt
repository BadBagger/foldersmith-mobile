package com.foldersmith.mobile.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.foldersmith.mobile.model.CleanupActionType
import com.foldersmith.mobile.ui.AppUiState

@Composable
fun OrganizedFolderScreen(state: AppUiState) {
    val archiveActions = state.cleanupActions.filter {
        it.actionType == CleanupActionType.Archive || it.actionType == CleanupActionType.Move
    }
    ScreenColumn {
        Text("Safe Archive")
        Text("FolderSmith Organized uses user-selected Android folders when applying moves or archives. File names are never overwritten; conflicts get a safe numbered name.")
        if (archiveActions.isEmpty()) {
            EmptyState("Nothing marked for archive", "Review a cleanup plan to choose files for the safe organized area.")
        } else {
            archiveActions.forEach { action ->
                val file = state.files.firstOrNull { it.id == action.fileId }
                if (file != null) {
                    FileRow(file)
                }
            }
        }
    }
}
