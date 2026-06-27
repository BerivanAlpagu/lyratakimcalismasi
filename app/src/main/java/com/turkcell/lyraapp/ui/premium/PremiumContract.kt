package com.turkcell.lyraapp.ui.premium

import com.turkcell.lyraapp.data.premium.PremiumPlan
import com.turkcell.lyraapp.data.premium.PremiumPlanType

data class PremiumUiState(
    val plans: List<PremiumPlan> = emptyList(),
    val selectedPlanType: PremiumPlanType = PremiumPlanType.Recurring,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PremiumIntent {
    data object LoadPlans : PremiumIntent
    data object BackClicked : PremiumIntent
    data object ContinueClicked : PremiumIntent
    data object RetryClicked : PremiumIntent
    data class PlanSelected(val type: PremiumPlanType) : PremiumIntent
}

sealed interface PremiumEffect {
    data object NavigateBack : PremiumEffect
    data class NavigateToPayment(val planType: PremiumPlanType) : PremiumEffect
    data class ShowError(val message: String) : PremiumEffect
}
