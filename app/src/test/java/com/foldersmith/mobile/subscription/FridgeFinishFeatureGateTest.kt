package com.foldersmith.mobile.subscription

import org.junit.Assert.assertTrue
import org.junit.Test

class FridgeFinishFeatureGateTest {
    @Test
    fun freeUserCanAddItemBeforeLimit() {
        val state = FridgeSubscriptionState(
            tier = SubscriptionTier.Free,
            fridgeItemCount = FridgeSubscriptionState.FREE_ITEM_LIMIT - 1
        )

        val result = FridgeFinishFeatureGate.gateAddItem(state)

        assertTrue(result is FeatureGateResult.Allowed)
    }

    @Test
    fun freeUserGetsUpgradePromptAtItemLimit() {
        val state = FridgeSubscriptionState(
            tier = SubscriptionTier.Free,
            fridgeItemCount = FridgeSubscriptionState.FREE_ITEM_LIMIT
        )

        val result = FridgeFinishFeatureGate.gateAddItem(state)

        assertTrue(result is FeatureGateResult.UpgradeRequired)
    }

    @Test
    fun plusUserCanAddItemsBeyondFreeLimit() {
        val state = FridgeSubscriptionState(
            tier = SubscriptionTier.Plus,
            fridgeItemCount = 200
        )

        val result = FridgeFinishFeatureGate.gateAddItem(state)

        assertTrue(result is FeatureGateResult.Allowed)
    }

    @Test
    fun plusOnlyFeatureRequiresUpgradeForFreeUser() {
        val state = FridgeSubscriptionState(tier = SubscriptionTier.Free)

        val result = FridgeFinishFeatureGate.gateFeature(
            feature = FridgeFeature.SmartGroceryList,
            state = state
        )

        assertTrue(result is FeatureGateResult.UpgradeRequired)
    }

    @Test
    fun basicFeatureIsAllowedForFreeUser() {
        val state = FridgeSubscriptionState(tier = SubscriptionTier.Free)

        val result = FridgeFinishFeatureGate.gateFeature(
            feature = FridgeFeature.BasicExpirationDates,
            state = state
        )

        assertTrue(result is FeatureGateResult.Allowed)
    }
}
