package com.turkcell.lyraapp.ui.premium

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.premium.PremiumPlanType
import com.turkcell.lyraapp.data.premium.PremiumRepository
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
class PremiumViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialPlanType = savedStateHandle.get<String>(ARG_PLAN_TYPE)
        ?.let { value -> PremiumPlanType.entries.firstOrNull { it.apiValue == value } }
        ?: PremiumPlanType.Recurring

    private val _uiState = MutableStateFlow(PremiumUiState(selectedPlanType = initialPlanType))
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PremiumEffect>(Channel.BUFFERED)
    val effect: Flow<PremiumEffect> = _effect.receiveAsFlow()

    init {
        onIntent(PremiumIntent.LoadPlans)
    }

    fun onIntent(intent: PremiumIntent) {
        when (intent) {
            PremiumIntent.LoadPlans -> loadPlans()
            PremiumIntent.BackClicked -> sendEffect(PremiumEffect.NavigateBack)
            PremiumIntent.ContinueClicked -> continueToPayment()
            PremiumIntent.RetryClicked -> loadPlans()
            is PremiumIntent.PlanSelected -> {
                _uiState.update { it.copy(selectedPlanType = intent.type) }
            }
        }
    }

    private fun loadPlans() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            premiumRepository.getPlans()
                .onSuccess { plans ->
                    val selected = if (plans.any { it.type == _uiState.value.selectedPlanType }) {
                        _uiState.value.selectedPlanType
                    } else {
                        plans.firstOrNull { it.type == PremiumPlanType.Recurring }?.type
                            ?: plans.firstOrNull()?.type
                            ?: PremiumPlanType.Recurring
                    }
                    _uiState.update {
                        it.copy(
                            plans = plans,
                            selectedPlanType = selected,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    val message = error.message ?: "Premium planlari alinamadi."
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                    _effect.send(PremiumEffect.ShowError(message))
                }
        }
    }

    private fun continueToPayment() {
        val state = _uiState.value
        if (state.plans.none { it.type == state.selectedPlanType }) return
        sendEffect(PremiumEffect.NavigateToPayment(state.selectedPlanType))
    }

    private fun sendEffect(effect: PremiumEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    companion object {
        const val ARG_PLAN_TYPE = "planType"
    }
}
