package com.familyfinance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.familyfinance.data.local.entity.AccountEntity
import com.familyfinance.data.local.entity.CategoryEntity
import com.familyfinance.data.local.entity.ProjectEntity
import com.familyfinance.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // Accounts
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Upsert
    suspend fun upsertAccount(account: AccountEntity): Long

    @Query("UPDATE accounts SET lastReconciledAt = :timestamp WHERE id = :accountId")
    suspend fun updateReconciliationTimestamp(accountId: Long, timestamp: Long)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM accounts WHERE name = :name LIMIT 1)")
    suspend fun isAccountNameTaken(name: String): Boolean

    // Categories
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getCategoriesFlow(): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM categories WHERE name = :name LIMIT 1)")
    suspend fun isCategoryNameTaken(name: String): Boolean

    // Projects
    @Query("SELECT * FROM projects ORDER BY name ASC")
    fun getProjectsFlow(): Flow<List<ProjectEntity>>

    @Upsert
    suspend fun upsertProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM projects WHERE name = :name LIMIT 1)")
    suspend fun isProjectNameTaken(name: String): Boolean

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getTransactionsByAccountFlow(accountId: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)
}
