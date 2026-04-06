package com.familyfinance.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AccountType {
    CASH, BANK, INVESTMENT, CREDIT_CARD
}

// @trace TASK-122
@Entity(
    tableName = "accounts",
    indices = [Index(value = ["name", "currency", "ownerLabel"], unique = true)]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currency: String,
    val color: Int,
    val ownerLabel: String? = null,
    val lastReconciledAt: Long? = null
)
