package com.foldersmith.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foldersmith.mobile.subscription.FridgeFinishPlans
import com.foldersmith.mobile.subscription.FridgeSubscriptionState

@Composable
fun FridgePlusPaywallScreen(
    subscriptionState: FridgeSubscriptionState,
    onStartPurchase: () -> Unit,
    onBack: () -> Unit
) {
    ScreenColumn {
        Text("Fridge Finish Plus", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Keep basic fridge tracking free. Upgrade only when you want more organization, planning, and insight tools.",
            style = MaterialTheme.typography.bodyLarge
        )

        SectionCard(title = "Free") {
            Text("For simple expiration tracking", fontWeight = FontWeight.SemiBold)
            BenefitList(FridgeFinishPlans.freeBenefits)
        }

        SectionCard(title = "Plus") {
            Text("For deeper fridge organization", fontWeight = FontWeight.SemiBold)
            BenefitList(FridgeFinishPlans.plusBenefits)
        }

        SectionCard(title = "Plus storage locations") {
            Text(FridgeFinishPlans.plusLocations.joinToString(", "))
        }

        SectionCard(title = "Billing status") {
            Text(
                "Google Play Billing is not connected in this build. The app will not start a real purchase or fake an upgrade until billing is implemented.",
                style = MaterialTheme.typography.bodyMedium
            )
            subscriptionState.billingMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onStartPurchase,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Check upgrade availability")
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Maybe later")
                }
            }
        }
    }
}

@Composable
private fun BenefitList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("-", color = MaterialTheme.colorScheme.primary)
                Text(item, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
