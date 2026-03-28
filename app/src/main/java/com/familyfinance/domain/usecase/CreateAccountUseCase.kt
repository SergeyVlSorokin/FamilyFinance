package com.familyfinance.domain.usecase

import com.familyfinance.domain.model.Account
import com.familyfinance.domain.model.Transaction
import com.familyfinance.domain.model.TransactionType
import com.familyfinance.domain.repository.FinanceRepository
import javax.inject.Inject

class CreateAccountUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(
        account: Account,
        openingBalanceCents: Long,
        openingBalanceTimestamp: Long? = null
    ): Result<Long> {
        return try {
            val accountId = repository.saveAccount(account)
            
            if (openingBalanceCents != 0L) {
                val openingTransaction = Transaction(
                    date = openingBalanceTimestamp ?: System.currentTimeMillis(),
                    amountCents = openingBalanceCents,
                    accountId = accountId,
                    categoryId = null,
                    projectId = null,
                    note = "Opening Balance",
                    type = TransactionType.OPENING_BALANCE,
                    currencyCode = account.currency
                )
                repository.saveTransaction(openingTransaction)
            }
            
            Result.success(accountId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
