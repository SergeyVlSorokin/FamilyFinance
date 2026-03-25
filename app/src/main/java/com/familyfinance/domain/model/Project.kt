package com.familyfinance.domain.model

data class Project(
    val id: Long = 0,
    val name: String,
    val color: Int,
    val startDate: Long? = null,
    val endDate: Long? = null
)
