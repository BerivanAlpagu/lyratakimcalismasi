package com.turkcell.lyraapp.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Register ekranının MVI ViewModel'i.
 *
 * Tek giriş noktası [onIntent]'tir. Durum [uiState] üzerinden gözlemlenir; tek seferlik
 * olaylar [effect] kanalından akar. Navigasyon kararları Effect'e dönüştürülür; ViewModel
 * içinde hiçbir Android/Compose/navigasyon bağımlılığı bulunmaz.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RegisterEffect>(Channel.BUFFERED)
    val effect: Flow<RegisterEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.FirstNameChanged -> updateForm { it.copy(firstName = intent.value) }
            is RegisterIntent.LastNameChanged -> updateForm { it.copy(lastName = intent.value) }
            is RegisterIntent.PhoneNumberChanged -> updateForm { it.copy(phoneNumber = intent.value) }
            is RegisterIntent.PasswordChanged -> updateForm { it.copy(password = intent.value) }
            is RegisterIntent.TermsAcceptedChanged -> updateForm { it.copy(isTermsAccepted = intent.value) }
            is RegisterIntent.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is RegisterIntent.Submit -> submit()
            is RegisterIntent.BackClicked -> sendEffect(RegisterEffect.NavigateBack)
            is RegisterIntent.LoginClicked -> sendEffect(RegisterEffect.NavigateToLogin)
        }
    }

    /** Form alanını günceller; güç göstergesi ve buton aktifliğini yeniden türetir. */
    private fun updateForm(transform: (RegisterUiState) -> RegisterUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(
                passwordStrength = updated.password.passwordStrength(),
                isRegisterEnabled = updated.isFormValid(),
            )
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isRegisterEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.register(
                firstName = state.firstName,
                lastName = state.lastName,
                phoneNumber = state.phoneNumber,
                password = state.password,
            )
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { _effect.send(RegisterEffect.NavigateToHome) }
                .onFailure { error ->
                    _effect.send(RegisterEffect.ShowError(error.message ?: "Kayıt başarısız."))
                }
        }
    }

    private fun sendEffect(effect: RegisterEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

/** Kayıt butonunun aktif olması için validasyon (ekran kuralı: en az 8 karakter, bir rakam). */
private fun RegisterUiState.isFormValid(): Boolean =
    firstName.isNotBlank() &&
        lastName.isNotBlank() &&
        phoneNumber.isNotBlank() &&
        password.isPasswordPolicyValid() &&
        isTermsAccepted

/** Şifre politikası: en az 8 karakter ve en az bir rakam. */
private fun String.isPasswordPolicyValid(): Boolean =
    length >= MIN_PASSWORD_LENGTH && any(Char::isDigit)

/**
 * Şifre gücünü 0..[RegisterUiState.PASSWORD_STRENGTH_MAX] aralığında türetir:
 * uzunluk, rakam ve harf ölçütlerinden her biri bir segment doldurur.
 */
private fun String.passwordStrength(): Int {
    if (isEmpty()) return 0
    var score = 0
    if (length >= MIN_PASSWORD_LENGTH) score++
    if (any(Char::isDigit)) score++
    if (any(Char::isLetter)) score++
    return score.coerceAtMost(RegisterUiState.PASSWORD_STRENGTH_MAX)
}

private const val MIN_PASSWORD_LENGTH = 8
