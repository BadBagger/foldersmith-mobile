package com.foldersmith.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foldersmith.mobile.ui.AppUiState
import com.foldersmith.mobile.ui.formatBytes
import com.foldersmith.mobile.ui.formatDateOrNever

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
    val hasPlan = state.cleanupActions.isNotEmpty()

    ScreenColumn {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("FolderSmith Mobile", style = MaterialTheme.typography.headlineSmall)
            Text("Safe phone clutter organizer", style = MaterialTheme.typography.bodyMedium)
        }

        SectionCard(
            title = if (hasPlan) "Plan ready" else "Start with a safe scan",
            body = if (hasPlan) {
                "FolderSmith found ${state.dashboard.filesScanned} files and built a preview. Nothing has moved or been deleted."
            } else {
                "Scan photos, screenshots, downloads, or one folder. You will see the plan before anything changes."
            }
        ) {
            Button(
                onClick = if (hasPlan) onReviewPlan else onStartScan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (hasPlan) "Review safe cleanup plan" else "Start safe scan")
            }
            if (hasPlan) {
                OutlinedButton(onClick = onStartScan, modifier = Modifier.fillMaxWidth()) {
                    Text("Scan something else")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Scanned", state.dashboard.filesScanned.toString(), Modifier.weight(1f))
            MetricCard("Potential space", state.dashboard.suggestedCleanupBytes.formatBytes(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Duplicates", state.dashboard.duplicatesFound.toString(), Modifier.weight(1f))
            MetricCard("Screenshots", state.dashboard.screenshotsFound.toString(), Modifier.weight(1f))
        }

        SectionCard(title = "Cleanup opportunities") {
            StatusPill("Downloads ${state.dashboard.downloadsFound}")
            StatusPill("Large files ${state.dashboard.largeFilesFound}")
            StatusPill("Last scan ${state.dashboard.lastScanDate.formatDateOrNever()}")
        }

        SectionCard(
            title = "Organize by destination",
            body = "Review suggested folders before applying anything. FolderSmith never overwrites files and does not auto-delete."
        ) {
            OutlinedButton(onClick = onOpenScreenshots, modifier = Modifier.fillMaxWidth()) { Text("Screenshots review") }
            OutlinedButton(onClick = onOpenDownloads, modifier = Modifier.fillMaxWidth()) { Text("Downloads review") }
            OutlinedButton(onClick = onOpenDuplicates, modifier = Modifier.fillMaxWidth()) { Text("Duplicate groups") }
            OutlinedButton(onClick = onOpenPhotoEvents, modifier = Modifier.fillMaxWidth()) { Text("Photo events") }
            OutlinedButton(onClick = onOpenOrganizedFolder, modifier = Modifier.fillMaxWidth()) { Text("FolderSmith Organized") }
        }
    }
}
