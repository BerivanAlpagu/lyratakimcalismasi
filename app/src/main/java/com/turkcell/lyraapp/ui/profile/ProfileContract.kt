package com.turkcell.lyraapp.ui.profile

import com.turkcell.lyraapp.data.profile.UserProfile

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDarkMode: Boolean = false,
    val showEditSheet: Boolean = false,
    val showRenewalReminder: Boolean = false,
    val membershipDaysRemaining: Int? = null,
)

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object LogoutClicked : ProfileIntent
    data class ThemeChanged(val isDark: Boolean) : ProfileIntent
    data object EditProfileClicked : ProfileIntent
    data object DismissEditSheet : ProfileIntent
    data class SaveProfile(val firstName: String, val lastName: String, val birthDate: String) : ProfileIntent
    data object PremiumCardClicked : ProfileIntent
    data object MonthlyRenewalClicked : ProfileIntent
    data object OneTimeRenewalClicked : ProfileIntent
    data object DismissRenewalReminder : ProfileIntent
}

sealed interface ProfileEffect {
    data class ShowError(val message: String) : ProfileEffect
    data object NavigateToLogin : ProfileEffect
    data class NavigateToPremium(val planType: String? = null) : ProfileEffect
}
