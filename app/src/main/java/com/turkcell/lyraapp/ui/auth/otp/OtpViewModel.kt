package com.turkcell.lyraapp.ui.auth.otp

import androidx.lifecycle.SavedStateHandle
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

@HiltViewModel
class OtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val phoneArg: String = checkNotNull(savedStateHandle["phone"])

    private val _uiState = MutableStateFlow(OtpUiState(phone = phoneArg))
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private val _effect = Channel<OtpEffect>(Channel.BUFFERED)
    val effect: Flow<OtpEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: OtpIntent) {
        when (intent) {
            is OtpIntent.CodeChanged -> {
                val newCode = intent.value.take(6).filter { it.isDigit() }
                _uiState.update { it.copy(code = newCode, isVerifyEnabled = newCode.length == 6) }
            }
            is OtpIntent.Submit -> submit()
            is OtpIntent.ResendCode -> resendCode()
            is OtpIntent.BackClicked -> viewModelScope.launch { _effect.send(OtpEffect.NavigateBack) }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isVerifyEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.verifyOtp(state.phone, state.code)
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess { session ->
                if (session.firstTime) {
                    _effect.send(OtpEffect.NavigateToProfileComplete)
                } else {
                    _effect.send(OtpEffect.NavigateToHome)
                }
            }.onFailure { error ->
                _effect.send(OtpEffect.ShowError(error.message ?: "Doğrulama başarısız."))
            }
        }
    }

    private fun resendCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.requestOtp(phoneArg)
            _uiState.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                _effect.send(OtpEffect.ShowMessage("Kod tekrar gönderildi."))
            }.onFailure { error ->
                _effect.send(OtpEffect.ShowError(error.message ?: "Kod gönderilemedi."))
            }
        }
    }
}
