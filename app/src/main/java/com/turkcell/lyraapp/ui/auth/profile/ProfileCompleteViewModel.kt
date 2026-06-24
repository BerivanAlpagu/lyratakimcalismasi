package com.turkcell.lyraapp.ui.auth.profile

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
class ProfileCompleteViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileCompleteUiState())
    val uiState: StateFlow<ProfileCompleteUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileCompleteEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileCompleteEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: ProfileCompleteIntent) {
        when (intent) {
            is ProfileCompleteIntent.FirstNameChanged -> updateForm { it.copy(firstName = intent.value) }
            is ProfileCompleteIntent.LastNameChanged -> updateForm { it.copy(lastName = intent.value) }
            is ProfileCompleteIntent.BirthDayChanged -> updateForm { it.copy(birthDay = intent.value.take(2).filter { c -> c.isDigit() }) }
            is ProfileCompleteIntent.BirthMonthChanged -> updateForm { it.copy(birthMonth = intent.value.take(2).filter { c -> c.isDigit() }) }
            is ProfileCompleteIntent.BirthYearChanged -> updateForm { it.copy(birthYear = intent.value.take(4).filter { c -> c.isDigit() }) }
            is ProfileCompleteIntent.Submit -> submit()
            is ProfileCompleteIntent.BackClicked -> viewModelScope.launch { _effect.send(ProfileCompleteEffect.NavigateBack) }
        }
    }

    private fun updateForm(transform: (ProfileCompleteUiState) -> ProfileCompleteUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isCompleteEnabled = updated.isFormValid())
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isCompleteEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Format YYYY-MM-DD
            val formattedDate = "${state.birthYear}-${state.birthMonth.padStart(2, '0')}-${state.birthDay.padStart(2, '0')}"
            
            val result = authRepository.updateProfile(state.firstName, state.lastName, formattedDate)
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess {
                _effect.send(ProfileCompleteEffect.NavigateToHome)
            }.onFailure { error ->
                _effect.send(ProfileCompleteEffect.ShowError(error.message ?: "Bilgiler güncellenemedi."))
            }
        }
    }
}

private fun ProfileCompleteUiState.isFormValid(): Boolean =
    firstName.isNotBlank() && lastName.isNotBlank() && 
    birthDay.isNotBlank() && birthMonth.isNotBlank() && birthYear.length == 4
