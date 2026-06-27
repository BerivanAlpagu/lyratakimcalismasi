package com.turkcell.lyraapp.ui.payment

import com.turkcell.lyraapp.data.premium.PremiumPlan
import com.turkcell.lyraapp.data.premium.PremiumPlanType

data class PaymentUiState(
    val selectedPlanType: PremiumPlanType = PremiumPlanType.Recurring,
    val selectedPlan: PremiumPlan? = null,
    val cardNumber: String = "",
    val holderName: String = "",
    val expiry: String = "",
    val cvc: String = "",
    val isLoading: Boolean = false,
    val isPayEnabled: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PaymentIntent {
    data object LoadPlan : PaymentIntent
    data object BackClicked : PaymentIntent
    data object PayClicked : PaymentIntent
    data class CardNumberChanged(val value: String) : PaymentIntent
    data class HolderNameChanged(val value: String) : PaymentIntent
    data class ExpiryChanged(val value: String) : PaymentIntent
    data class CvcChanged(val value: String) : PaymentIntent
}

sealed interface PaymentEffect {
    data object NavigateBack : PaymentEffect
    data object NavigateToSuccess : PaymentEffect
    data class ShowError(val message: String) : PaymentEffect
}
