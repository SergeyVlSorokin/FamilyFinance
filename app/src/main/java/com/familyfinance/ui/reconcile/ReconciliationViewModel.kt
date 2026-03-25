package com.familyfinance.ui.reconcile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyfinance.domain.usecase.AccountBalance
import com.familyfinance.domain.usecase.GetAccountBalancesUseCase
import com.familyfinance.domain.usecase.ReconcileAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReconciliationUiState(
    val accountBalance: AccountBalance? = null,
    val actualBalanceInput: String = "",
    val reconciliationTimeMillis: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val discrepancyCents: Long? = null
)

// @trace TASK-113

@HiltViewModel
class ReconciliationViewModel @Inject constructor(
    private val getAccountBalancesUseCase: GetAccountBalancesUseCase,
    private val reconcileAccountUseCase: ReconcileAccountUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: Long = checkNotNull(savedStateHandle["accountId"])

    private val _uiState = MutableStateFlow(ReconciliationUiState())
    val uiState: StateFlow<ReconciliationUiState> = _uiState.asStateFlow()

    init {
        loadAccount()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadAccount() {
        _uiState
            .map { it.reconciliationTimeMillis }
            .distinctUntilChanged()
            .flatMapLatest { timeMillis ->
                getAccountBalancesUseCase(timeMillis)
                    .map { balances -> balances.find { it.account.id == accountId } }
            }
            .onEach { accountBalance ->
                _uiState.update { it.copy(accountBalance = accountBalance) }
                updateDiscrepancy()
            }
            .launchIn(viewModelScope)
    }

    fun onTimeChange(newTimeMillis: Long) {
        _uiState.update { it.copy(reconciliationTimeMillis = newTimeMillis) }
    }

    fun onActualBalanceChange(input: String) {
        // Only allow numbers and decimal point
        if (input.isEmpty() || input.matches(Regex("^\\d*[.,]?\\d{0,2}$"))) {
            _uiState.update { it.copy(actualBalanceInput = input.replace(',', '.')) }
            updateDiscrepancy()
        }
    }

    private fun updateDiscrepancy() {
        val state = _uiState.value
        val recorded = state.accountBalance?.balanceCents ?: return
        val input = state.actualBalanceInput.toDoubleOrNull()
        
        if (input == null) {
            _uiState.update { it.copy(discrepancyCents = null) }
            return
        }

        val actualCents = (input * 100).toLong()
        _uiState.update { it.copy(discrepancyCents = actualCents - recorded) }
    }

    fun reconcile() {
        val state = _uiState.value
        val input = state.actualBalanceInput.toDoubleOrNull() ?: return
        val actualCents = (input * 100).toLong()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            reconcileAccountUseCase(
                accountId = accountId,
                actualBalanceCents = actualCents,
                date = state.reconciliationTimeMillis
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false, isSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
