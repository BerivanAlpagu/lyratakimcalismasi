package com.turkcell.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.profile.MembershipStatus
import com.turkcell.lyraapp.data.profile.MembershipType
import com.turkcell.lyraapp.data.profile.ProfileRepository
import com.turkcell.lyraapp.data.profile.UserMembership
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.turkcell.lyraapp.data.local.SettingsStore
import com.turkcell.lyraapp.data.local.TokenStore
import kotlinx.coroutines.flow.collectLatest

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val settingsStore: SettingsStore,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect: SharedFlow<ProfileEffect> = _effect.asSharedFlow()

    init {
        handleIntent(ProfileIntent.LoadProfile)
        viewModelScope.launch {
            settingsStore.isDarkMode.collectLatest { isDark ->
                _uiState.update { it.copy(isDarkMode = isDark ?: false) }
            }
        }
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.LogoutClicked -> performLogout()
            is ProfileIntent.ThemeChanged -> setTheme(intent.isDark)
            is ProfileIntent.EditProfileClicked -> _uiState.update { it.copy(showEditSheet = true) }
            is ProfileIntent.DismissEditSheet -> _uiState.update { it.copy(showEditSheet = false) }
            is ProfileIntent.SaveProfile -> saveProfile(intent.firstName, intent.lastName, intent.birthDate)
            is ProfileIntent.PremiumCardClicked -> navigateToPremium(null)
            is ProfileIntent.MonthlyRenewalClicked -> navigateToPremium("recurring")
            is ProfileIntent.OneTimeRenewalClicked -> navigateToPremium("one-time")
            is ProfileIntent.DismissRenewalReminder -> {
                _uiState.update { it.copy(showRenewalReminder = false) }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getProfile()
                .onSuccess { profile ->
                    val daysRemaining = profile.membership?.daysRemaining()
                    _uiState.update {
                        it.copy(
                            profile = profile,
                            isLoading = false,
                            membershipDaysRemaining = daysRemaining,
                            showRenewalReminder = profile.membership.shouldShowRenewalReminder(daysRemaining),
                        )
                    }
                }
                .onFailure { error ->
                    val errorMsg = error.message ?: "Bilinmeyen bir hata oluştu"
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                    _effect.emit(ProfileEffect.ShowError(errorMsg))
                }
        }
    }

    private fun performLogout() {
        viewModelScope.launch {
            tokenStore.clear()
            _effect.emit(ProfileEffect.NavigateToLogin)
        }
    }

    private fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsStore.setDarkMode(isDark)
        }
    }

    private fun saveProfile(firstName: String, lastName: String, birthDate: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, showEditSheet = false) }
            repository.updateProfile(firstName, lastName, birthDate)
                .onSuccess { profile ->
                    val daysRemaining = profile.membership?.daysRemaining()
                    _uiState.update {
                        it.copy(
                            profile = profile,
                            isLoading = false,
                            membershipDaysRemaining = daysRemaining,
                            showRenewalReminder = profile.membership.shouldShowRenewalReminder(daysRemaining),
                        )
                    }
                }
                .onFailure { error ->
                    val errorMsg = error.message ?: "Güncelleme sırasında bir hata oluştu"
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                    _effect.emit(ProfileEffect.ShowError(errorMsg))
                }
        }
    }

    private fun navigateToPremium(planType: String?) {
        viewModelScope.launch {
            _effect.emit(ProfileEffect.NavigateToPremium(planType))
        }
    }

    private fun UserMembership?.shouldShowRenewalReminder(daysRemaining: Int?): Boolean =
        this != null &&
            type == MembershipType.OneTime &&
            status == MembershipStatus.Active &&
            !autoRenew &&
            daysRemaining != null &&
            daysRemaining in 0..3

    private fun UserMembership.daysRemaining(): Int? {
        val expiresAtDate = ISO_FORMATS.firstNotNullOfOrNull { format ->
            runCatching { format.parse(expiresAt) }.getOrNull()
        } ?: return null
        val diffMs = expiresAtDate.time - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diffMs.coerceAtLeast(0L)).toInt()
    }

    companion object {
        private val ISO_FORMATS = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            },
        )
    }
}
