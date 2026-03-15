package com.familyfinance.domain.model

enum class CategoryType {
    EXPENSE, INCOME
}

data class Category(
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val color: Int
)
