package com.foldersmith.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foldersmith.mobile.subscription.FridgeFinishFeatureGate
import com.foldersmith.mobile.subscription.FridgeSubscriptionState
import com.foldersmith.mobile.subscription.FeatureGateResult

@Composable
fun SettingsScreen(
    subscriptionState: FridgeSubscriptionState,
    onOpenPlus: () -> Unit,
    onRestorePurchases: () -> Unit
) {
    ScreenColumn {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        SectionCard(title = "Fridge Finish Plus") {
            Text("Current plan: ${if (subscriptionState.isPlus) "Plus" else "Free"}")
            Text(
                "Free includes basic fridge tracking for up to ${FridgeSubscriptionState.FREE_ITEM_LIMIT} items. Plus adds deeper organization and planning tools.",
                style = MaterialTheme.typography.bodyMedium
            )
            when (val gate = FridgeFinishFeatureGate.gateAddItem(subscriptionState)) {
                FeatureGateResult.Allowed -> {
                    Text(
                        "Free item slots used: ${subscriptionState.fridgeItemCount}/${subscriptionState.itemLimit ?: "unlimited"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                is FeatureGateResult.UpgradeRequired -> {
                    Text(gate.title, fontWeight = FontWeight.SemiBold)
                    Text(gate.message, style = MaterialTheme.typography.bodyMedium)
                }
            }
            subscriptionState.billingMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(onClick = onOpenPlus, modifier = Modifier.weight(1f)) {
                    Text("View Plus")
                }
                OutlinedButton(onClick = onRestorePurchases, modifier = Modifier.weight(1f)) {
                    Text("Restore")
                }
            }
        }

        SectionCard(
            title = "Privacy",
            body = "FolderSmith Mobile organizes files locally on your device. It does not upload your photos or files. You stay in control of what is moved, archived, or deleted."
        )
        SectionCard(
            title = "Storage access",
            body = "The app uses Android Photo Picker, MediaStore, and Storage Access Framework patterns where appropriate. Broad manage-all-files permission is intentionally avoided for this MVP."
        )
        SectionCard(
            title = "Safety",
            body = "FolderSmith never deletes by default. Risky actions require a visible cleanup plan and explicit confirmation."
        )
    }
}
