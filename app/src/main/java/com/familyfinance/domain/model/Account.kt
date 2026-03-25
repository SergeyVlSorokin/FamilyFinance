package com.familyfinance.domain.model

enum class AccountType {
    CASH, BANK, INVESTMENT, CREDIT_CARD
}

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currency: String,
    val color: Int,
    val ownerLabel: String? = null,
    val lastReconciledAt: Long? = null
)
