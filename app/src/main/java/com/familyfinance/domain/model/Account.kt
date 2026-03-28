package com.familyfinance.domain.model

enum class AccountType {
    CASH, BANK, INVESTMENT, CREDIT_CARD
}

// @trace TASK-114, TASK-115, TASK-118
data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currency: String,
    val color: Int,
    val ownerLabel: String? = null,
    val lastReconciledAt: Long? = null
) {
    companion object {
        val SupportedCurrencies = listOf("EUR", "USD", "SEK", "GBP", "CHF", "JPY", "CAD", "AUD", "NOK", "DKK", "PLN")
    }
}
