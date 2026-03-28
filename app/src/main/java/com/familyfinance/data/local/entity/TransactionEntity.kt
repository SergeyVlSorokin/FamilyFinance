package com.familyfinance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER, OPENING_BALANCE, REVALUATION, RECONCILIATION_ADJUSTMENT
}

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetAccountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("accountId"),
        Index("categoryId"),
        Index("projectId"),
        Index("targetAccountId"),
        Index("receiptGroupId")
    ]
)
/**
 * Represents a financial transaction.
 * @trace TASK-114
 */
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long, // Epoch millis
    val amountCents: Long,
    val accountId: Long,
    val categoryId: Long?,
    val projectId: Long?,
    val note: String,
    val type: TransactionType,
    val currencyCode: String,
    val targetAccountId: Long? = null, // Used for transfers
    val targetAmountCents: Long? = null, // Used for FX transfers
    val receiptGroupId: String? = null,
    val transferLinkedId: String? = null
)
