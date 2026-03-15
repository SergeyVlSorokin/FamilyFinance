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
    suspend operator fun invoke(
        accountId: Long,
        actualBalanceCents: Long,
        date: Long
    ): Result<Long?> {
        val currentBalances = getAccountBalancesUseCase().first()
        val accountBalance = currentBalances.find { it.account.id == accountId }
            ?: return Result.failure(IllegalArgumentException("Account not found"))

        val discrepancy = actualBalanceCents - accountBalance.balanceCents
        if (discrepancy == 0L) return Result.success(null)

        val reconciliationTransaction = Transaction(
            date = date,
            amountCents = Math.abs(discrepancy),
            accountId = accountId,
            categoryId = null,
            projectId = null,
            note = "Reconciliation Adjustment",
            type = if (discrepancy > 0) TransactionType.INCOME else TransactionType.EXPENSE // Simplified for MVP
            // Ideally should use RECONCILIATION_ADJUSTMENT type and handle its sign in balance calc
        )

        return Result.success(repository.saveTransaction(reconciliationTransaction))
    }
}
