package com.turkcell.lyraapp.ui.premium

import com.turkcell.lyraapp.data.premium.PremiumPlan

data class PremiumState(
    val isLoading: Boolean = false,
    val plans: List<PremiumPlan> = emptyList(),
    val errorMessage: String? = null
)

sealed class PremiumIntent {
    object LoadPlans : PremiumIntent()
    data class PlanSelected(val planId: String) : PremiumIntent()
    object BackClicked : PremiumIntent()
}

sealed class PremiumEffect {
    data class NavigateToPayment(val planId: String) : PremiumEffect()
    object NavigateBack : PremiumEffect()
    data class ShowError(val message: String) : PremiumEffect()
}
