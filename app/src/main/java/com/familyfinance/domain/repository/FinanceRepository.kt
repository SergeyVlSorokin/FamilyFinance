package com.familyfinance.domain.repository

import com.familyfinance.domain.model.*
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    // Accounts
    fun getAccountsFlow(): Flow<List<Account>>
    suspend fun getAccountById(id: Long): Account?
    suspend fun saveAccount(account: Account): Long
    suspend fun updateAccountReconciliationDate(accountId: Long, timestamp: Long)
    suspend fun isAccountNameTaken(name: String): Boolean
    suspend fun deleteAccount(id: Long)

    // Categories
    fun getCategoriesFlow(): Flow<List<Category>>
    suspend fun saveCategory(category: Category)
    suspend fun isCategoryNameTaken(name: String): Boolean
    suspend fun deleteCategory(id: Long)

    // Projects
    fun getProjectsFlow(): Flow<List<Project>>
    suspend fun saveProject(project: Project)
    suspend fun isProjectNameTaken(name: String): Boolean
    suspend fun deleteProject(id: Long)

    // Transactions
    fun getTransactionsFlow(): Flow<List<Transaction>>
    fun getTransactionsByAccountFlow(accountId: Long): Flow<List<Transaction>>
    suspend fun saveTransaction(transaction: Transaction): Long
    suspend fun saveTransactions(transactions: List<Transaction>): List<Long>
    suspend fun deleteTransaction(id: Long)
}
