package com.foldersmith.mobile.subscription

enum class SubscriptionTier {
    Free,
    Plus
}

enum class FridgeFeature {
    BasicItemTracking,
    BasicExpirationDates,
    ExpiringSoonList,
    StandardCategories,
    UnlimitedItems,
    MultipleStorageLocations,
    CustomExpirationAlerts,
    SmartGroceryList,
    RecipeIdeas,
    WasteAndSavingsInsights,
    MealPlanningCalendar,
    BackupExport,
    PremiumThemes
}

data class FridgeSubscriptionState(
    val tier: SubscriptionTier = SubscriptionTier.Free,
    val fridgeItemCount: Int = 0,
    val billingMessage: String? = null
) {
    val itemLimit: Int? = if (tier == SubscriptionTier.Free) FREE_ITEM_LIMIT else null
    val isPlus: Boolean = tier == SubscriptionTier.Plus
    val isAtFreeItemLimit: Boolean = tier == SubscriptionTier.Free && fridgeItemCount >= FREE_ITEM_LIMIT

    companion object {
        const val FREE_ITEM_LIMIT = 25
    }
}

sealed class FeatureGateResult {
    data object Allowed : FeatureGateResult()
    data class UpgradeRequired(val title: String, val message: String) : FeatureGateResult()
}

object FridgeFinishFeatureGate {
    private val plusFeatures = setOf(
        FridgeFeature.UnlimitedItems,
        FridgeFeature.MultipleStorageLocations,
        FridgeFeature.CustomExpirationAlerts,
        FridgeFeature.SmartGroceryList,
        FridgeFeature.RecipeIdeas,
        FridgeFeature.WasteAndSavingsInsights,
        FridgeFeature.MealPlanningCalendar,
        FridgeFeature.BackupExport,
        FridgeFeature.PremiumThemes
    )

    fun gateAddItem(state: FridgeSubscriptionState): FeatureGateResult {
        return if (state.tier == SubscriptionTier.Free && state.fridgeItemCount >= FridgeSubscriptionState.FREE_ITEM_LIMIT) {
            FeatureGateResult.UpgradeRequired(
                title = "You reached 25 fridge items",
                message = "Basic fridge tracking stays free. Fridge Finish Plus removes the item limit when you need more room."
            )
        } else {
            FeatureGateResult.Allowed
        }
    }

    fun gateFeature(feature: FridgeFeature, state: FridgeSubscriptionState): FeatureGateResult {
        return if (feature in plusFeatures && state.tier != SubscriptionTier.Plus) {
            FeatureGateResult.UpgradeRequired(
                title = plusFeatureTitle(feature),
                message = "This planning feature is part of Fridge Finish Plus. Your basic fridge list still works without upgrading."
            )
        } else {
            FeatureGateResult.Allowed
        }
    }

    private fun plusFeatureTitle(feature: FridgeFeature): String {
        return when (feature) {
            FridgeFeature.UnlimitedItems -> "Unlimited fridge items"
            FridgeFeature.MultipleStorageLocations -> "Multiple storage locations"
            FridgeFeature.CustomExpirationAlerts -> "Custom expiration alerts"
            FridgeFeature.SmartGroceryList -> "Smart grocery list"
            FridgeFeature.RecipeIdeas -> "Recipe ideas"
            FridgeFeature.WasteAndSavingsInsights -> "Waste and savings insights"
            FridgeFeature.MealPlanningCalendar -> "Meal planning calendar"
            FridgeFeature.BackupExport -> "Backup and export"
            FridgeFeature.PremiumThemes -> "Premium themes"
            FridgeFeature.BasicItemTracking,
            FridgeFeature.BasicExpirationDates,
            FridgeFeature.ExpiringSoonList,
            FridgeFeature.StandardCategories -> "Included in Free"
        }
    }
}

object FridgeFinishPlans {
    val freeBenefits = listOf(
        "Track up to 25 fridge items",
        "Use 1 storage location",
        "Add, edit, and delete items",
        "See basic expiration dates",
        "See a basic expiring soon list",
        "Use standard categories"
    )

    val plusBenefits = listOf(
        "Unlimited fridge items",
        "Multiple storage locations",
        "Custom expiration alerts",
        "Smart grocery list",
        "Recipe ideas based on expiring ingredients",
        "Waste and savings insights",
        "Meal planning calendar",
        "Backup and export",
        "Premium themes"
    )

    val plusLocations = listOf(
        "Main Fridge",
        "Freezer",
        "Pantry",
        "Garage Freezer",
        "Mini Fridge",
        "Other"
    )
}
