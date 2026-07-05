package com.foldersmith.mobile.subscription

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface BillingGateway {
    val subscriptionTier: Flow<SubscriptionTier>
    suspend fun startPlusPurchase(): BillingResult
    suspend fun restorePurchases(): BillingResult
}

sealed class BillingResult {
    data object Unavailable : BillingResult()
    data object Pending : BillingResult()
    data class Failed(val reason: String) : BillingResult()
}

class PlaceholderBillingGateway : BillingGateway {
    override val subscriptionTier: Flow<SubscriptionTier> = flowOf(SubscriptionTier.Free)

    override suspend fun startPlusPurchase(): BillingResult {
        return BillingResult.Unavailable
    }

    override suspend fun restorePurchases(): BillingResult {
        return BillingResult.Unavailable
    }
}
