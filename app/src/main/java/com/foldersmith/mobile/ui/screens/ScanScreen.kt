package com.foldersmith.mobile.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
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
        Text("Safe scan")
        Text("Choose what to scan. FolderSmith asks only for access needed for the selected content and shows results before any action.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ScanType.entries.forEach { type ->
                FilterChip(
                    selected = selected.value == type,
                    onClick = { selected.value = type },
                    label = { Text(type.name.replace("SelectedFolder", "Folder")) }
                )
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            ScreenColumn {
                Text("What the app can access")
                Text("Photos and screenshots use Android media permissions. Downloads use MediaStore where Android allows it. Selected folders use Android's folder picker.")
                Text("What it cannot do")
                Text("It cannot delete, move, or archive anything until a cleanup plan is reviewed and confirmed.")
            }
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
                Text("Start selected scan")
            }
            Text(state.scanProgress.message)
        }
    }
}
