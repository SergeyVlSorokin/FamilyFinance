package com.familyfinance.domain.usecase

import com.familyfinance.domain.model.Transaction
import com.familyfinance.domain.model.TransactionType
import com.familyfinance.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ReconcileAccountUseCase @Inject constructor(
    private val repository: FinanceRepository,
    private val getAccountBalancesUseCase: GetAccountBalancesUseCase
) {
    // @trace TASK-113
    suspend operator fun invoke(
        accountId: Long,
        actualBalanceCents: Long,
        date: Long
    ): Result<Long?> {
        val currentBalances = getAccountBalancesUseCase(date).first()
        val accountBalance = currentBalances.find { it.account.id == accountId }
            ?: return Result.failure(IllegalArgumentException("Account not found"))

        val discrepancy = actualBalanceCents - accountBalance.balanceCents
        
        // Update account with reconciliation timestamp regardless of discrepancy
        repository.updateAccountReconciliationDate(accountId, date)

        if (discrepancy == 0L) return Result.success(null)

        val reconciliationTransaction = Transaction(
            date = date,
            amountCents = discrepancy, // Use signed amount for these internal types
            accountId = accountId,
            categoryId = null,
            projectId = null,
            note = if (accountBalance.account.type == com.familyfinance.domain.model.AccountType.INVESTMENT) "Investment Revaluation" else "Reconciliation Adjustment",
            type = if (accountBalance.account.type == com.familyfinance.domain.model.AccountType.INVESTMENT) 
                TransactionType.REVALUATION else TransactionType.RECONCILIATION_ADJUSTMENT,
            currencyCode = accountBalance.account.currency
        )

        return Result.success(repository.saveTransaction(reconciliationTransaction))
    }
}
