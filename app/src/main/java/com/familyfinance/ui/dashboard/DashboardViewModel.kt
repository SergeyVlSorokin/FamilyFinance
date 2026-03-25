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
    val totalWealthCents: Long = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getAccountBalancesUseCase: GetAccountBalancesUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = getAccountBalancesUseCase()
        .map { balances ->
            DashboardUiState(
                accounts = balances,
                totalWealthCents = balances.sumOf { it.balanceCents },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )
}
