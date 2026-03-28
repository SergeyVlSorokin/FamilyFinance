package com.familyfinance.domain.usecase

import com.familyfinance.domain.model.Account
import com.familyfinance.domain.model.Transaction
import com.familyfinance.domain.model.TransactionType
import com.familyfinance.domain.repository.FinanceRepository
import java.util.UUID
import javax.inject.Inject // @trace TASK-114, TASK-117

class SaveTransferUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(
        date: Long,
        amountCents: Long,
        fromAccount: Account,
        toAccount: Account,
        note: String,
        targetAmountCents: Long? = null
    ): Result<Long> {
        if (fromAccount.id == toAccount.id) {
            return Result.failure(IllegalArgumentException("Source and target accounts must be different"))
        }

        if (fromAccount.currency != toAccount.currency && targetAmountCents == null) {
            return Result.failure(IllegalArgumentException("Target amount is required for cross-currency transfers"))
        }

        val transferId = UUID.randomUUID().toString()
        val transaction = Transaction(
            date = date,
            amountCents = amountCents,
            accountId = fromAccount.id,
            targetAccountId = toAccount.id,
            categoryId = null,
            projectId = null,
            note = note,
            type = TransactionType.TRANSFER,
            currencyCode = fromAccount.currency,
            targetAmountCents = targetAmountCents,
            transferLinkedId = transferId
        )
        
        return Result.success(repository.saveTransaction(transaction))
    }
}
