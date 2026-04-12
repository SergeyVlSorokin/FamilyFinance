package com.familyfinance.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyfinance.domain.usecase.AccountBalance
import com.familyfinance.domain.usecase.GetAccountBalancesUseCase
import com.familyfinance.domain.model.Transaction
import com.familyfinance.domain.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val accounts: List<AccountBalance> = emptyList(),
    val totalWealth: Map<String, Long> = emptyMap(),
    val pendingReturns: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val refundContext: RefundContext? = null
)

data class RefundContext(
    val target: Transaction,
    val refundedSoFar: Long,
    val accountName: String,
    val categoryName: String,
    val projectName: String
)

// @trace TASK-120, TASK-203
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FinanceRepository,
    getAccountBalancesUseCase: GetAccountBalancesUseCase
) : ViewModel() {

    private val _refundContext = MutableStateFlow<RefundContext?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        getAccountBalancesUseCase(),
        repository.getPendingReturnsFlow(),
        _refundContext
    ) { balances, returns, refund ->
        DashboardUiState(
            accounts = balances,
            totalWealth = balances.groupBy { it.account.currency }
                .mapValues { (_, group) -> group.sumOf { it.balanceCents } },
            pendingReturns = returns,
            isLoading = false,
            refundContext = refund
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun initiateRefund(transaction: Transaction) {
        viewModelScope.launch {
            val account = repository.getAccountById(transaction.accountId)
            val category = transaction.categoryId?.let { repository.getCategoryById(it) }
            val project = transaction.projectId?.let { repository.getProjectById(it) }
            val sum = repository.getRefundsSum(transaction.id.toString())

            _refundContext.value = RefundContext(
                target = transaction,
                refundedSoFar = sum,
                accountName = account?.displayNameFullTuple ?: "Unknown Account",
                categoryName = category?.name ?: "No Category",
                projectName = project?.name ?: "No Project"
            )
        }
    }

    fun confirmRefund(amountCents: Long, moreRefundsFollow: Boolean) {
        val context = _refundContext.value ?: return
        viewModelScope.launch {
            // 1. Save the refund transaction (as a negative expense)
            val refund = context.target.copy(
                id = 0,
                date = System.currentTimeMillis(),
                amountCents = -kotlin.math.abs(amountCents),
                note = "Refund: ${context.target.note}",
                refundLinkedId = context.target.id.toString(),
                isReturnExpected = false // The refund itself doesn't expect a return
            )
            repository.saveTransaction(refund)

            // 2. Clear original flag if requested
            if (!moreRefundsFollow) {
                repository.dismissExpectedReturn(context.target.id)
            }
            
            _refundContext.value = null
        }
    }

    fun cancelRefund() {
        _refundContext.value = null
    }

    fun dismissReturn(transactionId: Long) {
        viewModelScope.launch {
            repository.dismissExpectedReturn(transactionId) // @trace TASK-203
        }
    }
}
