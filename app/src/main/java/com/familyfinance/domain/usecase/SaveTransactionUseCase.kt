package com.familyfinance.domain.usecase

import com.familyfinance.domain.model.Transaction
import com.familyfinance.domain.repository.FinanceRepository
import javax.inject.Inject

class SaveTransactionUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Long> {
        if (transaction.amountCents <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be positive"))
        }
        if (transaction.accountId == 0L) {
            return Result.failure(IllegalArgumentException("Account is mandatory"))
        }
        return Result.success(repository.saveTransaction(transaction))
    }
}
