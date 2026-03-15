package com.familyfinance.domain.usecase

import com.familyfinance.domain.model.Account
import com.familyfinance.domain.model.TransactionType
import com.familyfinance.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class AccountBalance(
    val account: Account,
    val balanceCents: Long
)

class GetAccountBalancesUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    operator fun invoke(): Flow<List<AccountBalance>> {
        return combine(
            repository.getAccountsFlow(),
            repository.getTransactionsFlow()
        ) { accounts, transactions ->
            accounts.map { account ->
                val balance = transactions.filter { it.accountId == account.id || it.targetAccountId == account.id }
                    .sumOf { transaction ->
                        calculateContribution(account.id, transaction)
                    }
                AccountBalance(account, balance)
            }
        }
    }

    private fun calculateContribution(accountId: Long, transaction: com.familyfinance.domain.model.Transaction): Long {
        return when (transaction.type) {
            TransactionType.INCOME -> if (transaction.accountId == accountId) transaction.amountCents else 0
            TransactionType.EXPENSE -> if (transaction.accountId == accountId) -transaction.amountCents else 0
            TransactionType.OPENING_BALANCE -> if (transaction.accountId == accountId) transaction.amountCents else 0
            TransactionType.REVALUATION -> if (transaction.accountId == accountId) transaction.amountCents else 0
            TransactionType.RECONCILIATION_ADJUSTMENT -> if (transaction.accountId == accountId) transaction.amountCents else 0
            TransactionType.TRANSFER -> {
                when {
                    transaction.accountId == accountId -> -transaction.amountCents // Outgoing
                    transaction.targetAccountId == accountId -> transaction.amountCents // Incoming
                    else -> 0
                }
            }
        }
    }
}
