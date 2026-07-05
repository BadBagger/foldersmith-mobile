package com.foldersmith.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foldersmith.mobile.ui.AppUiState
import com.foldersmith.mobile.ui.formatBytes
import com.foldersmith.mobile.ui.formatDateOrNever

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: AppUiState,
    onStartScan: () -> Unit,
    onReviewPlan: () -> Unit,
    onOpenDuplicates: () -> Unit,
    onOpenScreenshots: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPhotoEvents: () -> Unit,
    onOpenOrganizedFolder: () -> Unit
) {
    ScreenColumn {
        Text("FolderSmith Mobile")
        Text("A safe organizer for phone clutter.")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard("Files scanned", state.dashboard.filesScanned.toString(), Modifier.weight(1f))
            MetricCard("Duplicates found", state.dashboard.duplicatesFound.toString(), Modifier.weight(1f))
            MetricCard("Screenshots found", state.dashboard.screenshotsFound.toString(), Modifier.weight(1f))
            MetricCard("Downloads found", state.dashboard.downloadsFound.toString(), Modifier.weight(1f))
            MetricCard("Large files found", state.dashboard.largeFilesFound.toString(), Modifier.weight(1f))
            MetricCard("Suggested cleanup", state.dashboard.suggestedCleanupBytes.formatBytes(), Modifier.weight(1f))
        }
        Text("Last scan: ${state.dashboard.lastScanDate.formatDateOrNever()}")
        Button(onClick = onStartScan, modifier = Modifier.fillMaxWidth()) {
            Text("Start safe scan")
        }
        OutlinedButton(onClick = onReviewPlan, modifier = Modifier.fillMaxWidth()) {
            Text("Review cleanup plan")
        }
        OutlinedButton(onClick = onOpenDuplicates, modifier = Modifier.fillMaxWidth()) { Text("Duplicates") }
        OutlinedButton(onClick = onOpenScreenshots, modifier = Modifier.fillMaxWidth()) { Text("Screenshots") }
        OutlinedButton(onClick = onOpenDownloads, modifier = Modifier.fillMaxWidth()) { Text("Downloads") }
        OutlinedButton(onClick = onOpenPhotoEvents, modifier = Modifier.fillMaxWidth()) { Text("Photo Events") }
        OutlinedButton(onClick = onOpenOrganizedFolder, modifier = Modifier.fillMaxWidth()) { Text("Safe Archive") }
    }
}
