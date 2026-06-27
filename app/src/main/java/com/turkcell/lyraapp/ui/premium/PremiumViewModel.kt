package com.turkcell.lyraapp.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class PremiumViewModel @Inject constructor(
    private val repository: PremiumRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PremiumState())
    val state: StateFlow<PremiumState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<PremiumEffect>()
    val effect: SharedFlow<PremiumEffect> = _effect.asSharedFlow()

    init {
        onIntent(PremiumIntent.LoadPlans)
    }

    fun onIntent(intent: PremiumIntent) {
        when (intent) {
            is PremiumIntent.LoadPlans -> loadPlans()
            is PremiumIntent.PlanSelected -> handlePlanSelected(intent.planId)
            is PremiumIntent.BackClicked -> handleBackClicked()
        }
    }

    private fun loadPlans() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getPremiumPlans().onSuccess { plans ->
                _state.update { it.copy(isLoading = false, plans = plans) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                _effect.emit(PremiumEffect.ShowError(error.message ?: "Beklenmeyen bir hata oluştu."))
            }
        }
    }

    private fun handlePlanSelected(planId: String) {
        viewModelScope.launch {
            _effect.emit(PremiumEffect.NavigateToPayment(planId))
        }
    }

    private fun handleBackClicked() {
        viewModelScope.launch {
            _effect.emit(PremiumEffect.NavigateBack)
        }
    }
}
