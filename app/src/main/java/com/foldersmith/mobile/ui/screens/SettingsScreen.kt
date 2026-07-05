package com.foldersmith.mobile.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen() {
    ScreenColumn {
        Text("Settings")
        Text("Privacy")
        Text("FolderSmith Mobile organizes files locally on your device. It does not upload your photos or files. You stay in control of what is moved, archived, or deleted.")
        Text("Storage access")
        Text("The app uses Android Photo Picker, MediaStore, and Storage Access Framework patterns where appropriate. Broad manage-all-files permission is intentionally avoided for this MVP.")
        Text("Safety")
        Text("FolderSmith never deletes by default. Risky actions require a visible cleanup plan and explicit confirmation.")
    }
}
