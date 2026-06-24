package com.turkcell.lyraapp.ui.auth.profile

data class ProfileCompleteUiState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDay: String = "",
    val birthMonth: String = "",
    val birthYear: String = "",
    val isLoading: Boolean = false,
    val isCompleteEnabled: Boolean = false,
)

sealed interface ProfileCompleteIntent {
    data class FirstNameChanged(val value: String) : ProfileCompleteIntent
    data class LastNameChanged(val value: String) : ProfileCompleteIntent
    data class BirthDayChanged(val value: String) : ProfileCompleteIntent
    data class BirthMonthChanged(val value: String) : ProfileCompleteIntent
    data class BirthYearChanged(val value: String) : ProfileCompleteIntent
    data object Submit : ProfileCompleteIntent
    data object BackClicked : ProfileCompleteIntent
}

sealed interface ProfileCompleteEffect {
    data object NavigateToHome : ProfileCompleteEffect
    data object NavigateBack : ProfileCompleteEffect
    data class ShowError(val message: String) : ProfileCompleteEffect
}
