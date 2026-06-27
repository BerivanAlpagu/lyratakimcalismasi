package com.turkcell.lyraapp.ui.payment

data class PaymentState(
    val isLoading: Boolean = false,
    val planId: String = "",
    val cardNumber: String = "",
    val expMonth: String = "",
    val expYear: String = "",
    val cvc: String = "",
    val holderName: String = "",
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

sealed class PaymentIntent {
    data class PlanIdReceived(val planId: String) : PaymentIntent()
    data class CardNumberChanged(val number: String) : PaymentIntent()
    data class ExpMonthChanged(val month: String) : PaymentIntent()
    data class ExpYearChanged(val year: String) : PaymentIntent()
    data class CvcChanged(val cvc: String) : PaymentIntent()
    data class HolderNameChanged(val name: String) : PaymentIntent()
    object SubmitPayment : PaymentIntent()
    object BackClicked : PaymentIntent()
}

sealed class PaymentEffect {
    object NavigateBack : PaymentEffect()
    object PaymentSuccessful : PaymentEffect()
    data class ShowError(val message: String) : PaymentEffect()
}
