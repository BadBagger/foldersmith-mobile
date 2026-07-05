package com.foldersmith.mobile.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foldersmith.mobile.model.ScanType
import com.foldersmith.mobile.ui.AppUiState

@Composable
fun ScanScreen(
    state: AppUiState,
    onStartScan: (ScanType, String?) -> Unit,
    onCancel: () -> Unit
) {
    val selected = remember { mutableStateOf(ScanType.Photos) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { it }) {
            onStartScan(selected.value, null)
        }
    }
    val treeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            onStartScan(ScanType.SelectedFolder, uri.toString())
        }
    }
    val permissions = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    ScreenColumn {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Safe scan", style = MaterialTheme.typography.headlineSmall)
            Text("Pick one source. FolderSmith builds a preview before anything changes.")
        }

        SectionCard(title = "1. Choose source") {
            ScanChoice("Photos", selected.value == ScanType.Photos) { selected.value = ScanType.Photos }
            ScanChoice("Screenshots", selected.value == ScanType.Screenshots) { selected.value = ScanType.Screenshots }
            ScanChoice("Downloads", selected.value == ScanType.Downloads) { selected.value = ScanType.Downloads }
            ScanChoice("Selected folder", selected.value == ScanType.SelectedFolder) { selected.value = ScanType.SelectedFolder }
        }

        SectionCard(
            title = "2. What FolderSmith will build",
            body = when (selected.value) {
                ScanType.Photos -> "Photo events, exact duplicates, and large media worth reviewing."
                ScanType.Screenshots -> "Screenshot groups with a destination like FolderSmith Organized/Screenshots/Review."
                ScanType.Downloads -> "Downloads grouped into APKs, archives, PDFs, documents, and general files."
                ScanType.SelectedFolder -> "A safe plan for the folder you choose through Android's folder picker."
            }
        )

        SectionCard(
            title = "3. Next step",
            body = if (state.cleanupActions.isEmpty()) {
                "After the scan, go to Review to approve or change the cleanup plan."
            } else {
                "Results are ready. Open Review to inspect destinations and choose what to apply."
            }
        ) {
            StatusPill("${state.dashboard.filesScanned} files in latest plan")
            StatusPill("${state.dashboard.duplicatesFound} duplicate candidates")
        }

        if (state.isScanning) {
            LinearProgressIndicator(
                progress = {
                    if (state.scanProgress.total == 0) 0f else state.scanProgress.completed.toFloat() / state.scanProgress.total.toFloat()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Text(state.scanProgress.message)
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel scan")
            }
        } else {
            Button(
                onClick = {
                    if (selected.value == ScanType.SelectedFolder) {
                        treeLauncher.launch(null)
                    } else {
                        permissionLauncher.launch(permissions)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan selected source")
            }
            Text(state.scanProgress.message)
        }
    }
}

@Composable
private fun ScanChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}
