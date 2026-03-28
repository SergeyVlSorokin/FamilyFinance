package com.familyfinance.domain.usecase

import com.familyfinance.domain.model.Transaction
import com.familyfinance.domain.model.TransactionType
import com.familyfinance.domain.repository.FinanceRepository
import java.util.UUID
import javax.inject.Inject

class SaveTransferUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(
        date: Long,
        amountCents: Long,
        fromAccountId: Long,
        toAccountId: Long,
        note: String
    ): Result<Long> {
        if (fromAccountId == toAccountId) {
            return Result.failure(IllegalArgumentException("Source and target accounts must be different"))
        }

        val account = repository.getAccountById(fromAccountId)
            ?: return Result.failure(IllegalArgumentException("Account not found"))

        val transferId = UUID.randomUUID().toString()
        val transaction = Transaction(
            date = date,
            amountCents = amountCents,
            accountId = fromAccountId,
            targetAccountId = toAccountId,
            categoryId = null,
            projectId = null,
            note = note,
            type = TransactionType.TRANSFER,
            currencyCode = account.currency,
            transferLinkedId = transferId
        )

        // Repository saveTransaction is already marked @Transaction in DAO for single inserts,
        // but for transfers we rely on the DAO @Transaction insertTransactionAndUpdateBalance 
        // which I removed/simplified. 
        // Actually, Room handles single TransactionEntity as one row. 
        // In our model, a Transfer IS a single row with accountId and targetAccountId.
        
        return Result.success(repository.saveTransaction(transaction))
    }
}
