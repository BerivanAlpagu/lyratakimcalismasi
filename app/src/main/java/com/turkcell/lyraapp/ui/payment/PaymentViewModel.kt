package com.turkcell.lyraapp.ui.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.premium.PremiumCard
import com.turkcell.lyraapp.data.premium.PremiumPlanType
import com.turkcell.lyraapp.data.premium.PremiumRepository
import com.turkcell.lyraapp.ui.premium.PremiumViewModel
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
class PaymentViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val selectedPlanType = savedStateHandle.get<String>(PremiumViewModel.ARG_PLAN_TYPE)
        ?.let { value -> PremiumPlanType.entries.firstOrNull { it.apiValue == value } }
        ?: PremiumPlanType.Recurring

    private val _uiState = MutableStateFlow(PaymentUiState(selectedPlanType = selectedPlanType))
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PaymentEffect>(Channel.BUFFERED)
    val effect: Flow<PaymentEffect> = _effect.receiveAsFlow()

    init {
        onIntent(PaymentIntent.LoadPlan)
    }

    fun onIntent(intent: PaymentIntent) {
        when (intent) {
            PaymentIntent.LoadPlan -> loadPlan()
            PaymentIntent.BackClicked -> sendEffect(PaymentEffect.NavigateBack)
            PaymentIntent.PayClicked -> pay()
            is PaymentIntent.CardNumberChanged -> updateForm {
                it.copy(cardNumber = formatCardNumber(intent.value))
            }
            is PaymentIntent.HolderNameChanged -> updateForm {
                it.copy(holderName = intent.value.take(48))
            }
            is PaymentIntent.ExpiryChanged -> updateForm {
                it.copy(expiry = formatExpiry(intent.value))
            }
            is PaymentIntent.CvcChanged -> updateForm {
                it.copy(cvc = intent.value.filter(Char::isDigit).take(4))
            }
        }
    }

    private fun loadPlan() {
        if (_uiState.value.selectedPlan != null || _uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            premiumRepository.getPlans()
                .onSuccess { plans ->
                    val plan = plans.firstOrNull { it.type == selectedPlanType }
                    _uiState.update {
                        it.copy(
                            selectedPlan = plan,
                            isLoading = false,
                            errorMessage = if (plan == null) "Secili plan bulunamadi." else null,
                        ).withValidation()
                    }
                }
                .onFailure { error ->
                    val message = error.message ?: "Plan bilgisi alinamadi."
                    _uiState.update { it.copy(isLoading = false, errorMessage = message).withValidation() }
                    _effect.send(PaymentEffect.ShowError(message))
                }
        }
    }

    private fun pay() {
        val state = _uiState.value
        if (!state.isPayEnabled || state.isLoading) return
        val expiryParts = state.expiry.split("/")
        val expMonth = expiryParts.getOrNull(0)?.toIntOrNull() ?: return
        val expYearShort = expiryParts.getOrNull(1)?.toIntOrNull() ?: return
        val expYear = 2000 + expYearShort

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null).withValidation() }
            premiumRepository.checkout(
                planType = state.selectedPlanType,
                card = PremiumCard(
                    number = state.cardNumber,
                    expMonth = expMonth,
                    expYear = expYear,
                    cvc = state.cvc,
                    holderName = state.holderName,
                ),
            )
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false).withValidation() }
                    _effect.send(PaymentEffect.NavigateToSuccess)
                }
                .onFailure { error ->
                    val message = error.message ?: "Odeme tamamlanamadi."
                    _uiState.update { it.copy(isLoading = false, errorMessage = message).withValidation() }
                    _effect.send(PaymentEffect.ShowError(message))
                }
        }
    }

    private fun updateForm(transform: (PaymentUiState) -> PaymentUiState) {
        _uiState.update { transform(it).withValidation() }
    }

    private fun PaymentUiState.withValidation(): PaymentUiState =
        copy(
            isPayEnabled = selectedPlan != null &&
                cardNumber.filter(Char::isDigit).length == 16 &&
                holderName.isNotBlank() &&
                expiry.isValidExpiry() &&
                cvc.length in 3..4 &&
                !isLoading,
        )

    private fun formatCardNumber(value: String): String =
        value
            .filter(Char::isDigit)
            .take(16)
            .chunked(4)
            .joinToString(" ")

    private fun formatExpiry(value: String): String {
        val digits = value.filter(Char::isDigit).take(4)
        return if (digits.length <= 2) digits else "${digits.take(2)}/${digits.drop(2)}"
    }

    private fun String.isValidExpiry(): Boolean {
        val parts = split("/")
        val month = parts.getOrNull(0)?.toIntOrNull() ?: return false
        val year = parts.getOrNull(1)?.toIntOrNull() ?: return false
        return month in 1..12 && year >= 26
    }

    private fun sendEffect(effect: PaymentEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
