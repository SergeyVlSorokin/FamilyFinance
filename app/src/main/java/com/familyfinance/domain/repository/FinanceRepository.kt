package com.familyfinance.domain.repository

import com.familyfinance.domain.model.*
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    // Accounts
    fun getAccountsFlow(): Flow<List<Account>>
    suspend fun getAccountById(id: Long): Account?
    suspend fun saveAccount(account: Account): Long
    suspend fun deleteAccount(id: Long)

    // Categories
    fun getCategoriesFlow(): Flow<List<Category>>
    suspend fun saveCategory(category: Category)

    // Projects
    fun getProjectsFlow(): Flow<List<Project>>
    suspend fun saveProject(project: Project)

    // Transactions
    fun getTransactionsFlow(): Flow<List<Transaction>>
    fun getTransactionsByAccountFlow(accountId: Long): Flow<List<Transaction>>
    suspend fun saveTransaction(transaction: Transaction): Long
    suspend fun saveTransactions(transactions: List<Transaction>): List<Long>
    suspend fun deleteTransaction(id: Long)
}
