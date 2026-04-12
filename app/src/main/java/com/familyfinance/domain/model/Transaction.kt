package com.familyfinance.domain.model

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER, OPENING_BALANCE, REVALUATION, RECONCILIATION_ADJUSTMENT // @trace TASK-114, TASK-115
}

data class Transaction(
    val id: Long = 0,
    val date: Long,
    val amountCents: Long,
    val accountId: Long,
    val categoryId: Long?,
    val projectId: Long?,
    val note: String,
    val type: TransactionType,
    val currencyCode: String,
    val targetAccountId: Long? = null,
    val targetAmountCents: Long? = null,
    val receiptGroupId: String? = null,
    val transferLinkedId: String? = null,
    val isReturnExpected: Boolean = false,
    val refundLinkedId: String? = null
) // @trace TASK-202
