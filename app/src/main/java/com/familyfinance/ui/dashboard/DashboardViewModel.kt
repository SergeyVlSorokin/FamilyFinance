package com.familyfinance.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyfinance.domain.usecase.AccountBalance
import com.familyfinance.domain.usecase.GetAccountBalancesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val accounts: List<AccountBalance> = emptyList(),
    val totalWealth: Map<String, Long> = emptyMap(),
    val isLoading: Boolean = true
)

// @trace TASK-120
@HiltViewModel
class DashboardViewModel @Inject constructor(
    getAccountBalancesUseCase: GetAccountBalancesUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = getAccountBalancesUseCase()
        .map { balances ->
            DashboardUiState(
                accounts = balances,
                totalWealth = balances.groupBy { it.account.currency }
                    .mapValues { (_, group) -> group.sumOf { it.balanceCents } },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )
}
