package com.familyfinance.domain.usecase

import com.familyfinance.domain.model.Transaction
import com.familyfinance.domain.model.TransactionType
import com.familyfinance.domain.repository.FinanceRepository
import java.util.UUID
import javax.inject.Inject

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
                note = if (it.note.isEmpty()) "Split Receipt ($receiptGroupId)" else "${it.note} (Split: $receiptGroupId)",
                type = TransactionType.EXPENSE // Split receipts are typically expenses
            )
        }

        return Result.success(repository.saveTransactions(processedSplits))
    }
}
