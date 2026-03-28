package com.familyfinance.domain.usecase

import com.familyfinance.domain.model.Transaction
import com.familyfinance.domain.model.TransactionType
import com.familyfinance.domain.repository.FinanceRepository
import java.util.UUID
import javax.inject.Inject // @trace TASK-114, TASK-116
class SaveSplitReceiptUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(
        totalAmountCents: Long,
        splits: List<Transaction>
    ): Result<List<Long>> {
        val splitSum = splits.sumOf { it.amountCents }
        if (splitSum > totalAmountCents) {
            return Result.failure(IllegalArgumentException("Split sum ($splitSum) exceeds total ($totalAmountCents)"))
        }

        val receiptGroupId = UUID.randomUUID().toString()
        val processedSplits = splits.map { 
            it.copy(
                receiptGroupId = receiptGroupId,
                note = if (it.note.isNotBlank()) "Split: ${it.note}" else "Split",
                type = TransactionType.EXPENSE // Split receipts are typically expenses
            )
        }

        return Result.success(repository.saveTransactions(processedSplits))
    }
}
