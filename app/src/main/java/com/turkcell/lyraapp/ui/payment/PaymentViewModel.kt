package com.turkcell.lyraapp.ui.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.premium.CardDetailsDto
import com.turkcell.lyraapp.data.premium.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: PremiumRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentState())
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<PaymentEffect>()
    val effect: SharedFlow<PaymentEffect> = _effect.asSharedFlow()

    init {
        savedStateHandle.get<String>("planId")?.let { planId ->
            onIntent(PaymentIntent.PlanIdReceived(planId))
        }
    }

    fun onIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.PlanIdReceived -> {
                _state.update { it.copy(planId = intent.planId) }
            }
            is PaymentIntent.CardNumberChanged -> _state.update { it.copy(cardNumber = intent.number) }
            is PaymentIntent.ExpMonthChanged -> _state.update { it.copy(expMonth = intent.month) }
            is PaymentIntent.ExpYearChanged -> _state.update { it.copy(expYear = intent.year) }
            is PaymentIntent.CvcChanged -> _state.update { it.copy(cvc = intent.cvc) }
            is PaymentIntent.HolderNameChanged -> _state.update { it.copy(holderName = intent.name) }
            is PaymentIntent.SubmitPayment -> submitPayment()
            is PaymentIntent.BackClicked -> handleBackClicked()
        }
    }

    private fun submitPayment() {
        val currentState = _state.value
        
        val expMonth = currentState.expMonth.toIntOrNull() ?: 0
        val expYear = currentState.expYear.toIntOrNull() ?: 0

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.checkout(
                planId = currentState.planId,
                card = CardDetailsDto(
                    number = currentState.cardNumber,
                    expMonth = expMonth,
                    expYear = expYear,
                    cvc = currentState.cvc,
                    holderName = currentState.holderName
                )
            ).onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
                _effect.emit(PaymentEffect.PaymentSuccessful)
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                _effect.emit(PaymentEffect.ShowError(error.message ?: "Ödeme başarısız."))
            }
        }
    }

    private fun handleBackClicked() {
        viewModelScope.launch {
            _effect.emit(PaymentEffect.NavigateBack)
        }
    }
}
