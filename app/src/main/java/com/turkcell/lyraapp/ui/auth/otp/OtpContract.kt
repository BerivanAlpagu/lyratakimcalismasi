package com.turkcell.lyraapp.ui.auth.otp

data class OtpUiState(
    val code: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val isVerifyEnabled: Boolean = false,
)

sealed interface OtpIntent {
    data class CodeChanged(val value: String) : OtpIntent
    data object Submit : OtpIntent
    data object ResendCode : OtpIntent
    data object BackClicked : OtpIntent
}

sealed interface OtpEffect {
    data object NavigateToHome : OtpEffect
    data object NavigateToProfileComplete : OtpEffect
    data object NavigateBack : OtpEffect
    data class ShowError(val message: String) : OtpEffect
    data class ShowMessage(val message: String) : OtpEffect
}
