package com.familyfinance.domain.usecase

import com.familyfinance.domain.model.*
import com.familyfinance.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveSplitReceiptUseCaseTest {

    private lateinit var repository: FakeFinanceRepository
    private lateinit var useCase: SaveSplitReceiptUseCase

    @Before
    fun setUp() {
        repository = FakeFinanceRepository()
        useCase = SaveSplitReceiptUseCase(repository)
    }

    @Test
    fun `validate split sum does not exceed total`() = runBlocking {
        // Given
        val splits = listOf(
            Transaction(amountCents = 600, accountId = 1, categoryId = 1, projectId = null, date = 0, note = "", type = TransactionType.EXPENSE, currencyCode = "USD"),
            Transaction(amountCents = 500, accountId = 1, categoryId = 2, projectId = null, date = 0, note = "", type = TransactionType.EXPENSE, currencyCode = "USD")
        )

        // When
        val result = useCase(1000, splits)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Split sum (1100) exceeds total (1000)", result.exceptionOrNull()?.message)
    }

    @Test
    fun `generate receipt group id and save splits`() = runBlocking {
        // Given
        val splits = listOf(
            Transaction(amountCents = 400, accountId = 1, categoryId = 1, projectId = null, date = 0, note = "Milk", type = TransactionType.EXPENSE, currencyCode = "USD"),
            Transaction(amountCents = 600, accountId = 1, categoryId = 2, projectId = null, date = 0, note = "Bread", type = TransactionType.EXPENSE, currencyCode = "USD")
        )

        // When
        val result = useCase(1000, splits)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, repository.savedTransactions.size)
        assertTrue(repository.savedTransactions.all { it.receiptGroupId != null })
        assertEquals(repository.savedTransactions[0].receiptGroupId, repository.savedTransactions[1].receiptGroupId)
    }

    private class FakeFinanceRepository : FinanceRepository {
        val savedTransactions = mutableListOf<Transaction>()

        override fun getAccountsFlow(): Flow<List<Account>> = MutableStateFlow(emptyList())
        override suspend fun getAccountById(id: Long): Account? = null
        override suspend fun saveAccount(account: Account): Long = 0
        override suspend fun updateAccountReconciliationDate(accountId: Long, timestamp: Long) {}
        override suspend fun isAccountNameTaken(name: String): Boolean = false
        override suspend fun deleteAccount(id: Long) {}
        override fun getCategoriesFlow(): Flow<List<Category>> = MutableStateFlow(emptyList())
        override suspend fun saveCategory(category: Category) {}
        override suspend fun isCategoryNameTaken(name: String): Boolean = false
        override suspend fun deleteCategory(id: Long) {}
        override fun getProjectsFlow(): Flow<List<Project>> = MutableStateFlow(emptyList())
        override suspend fun saveProject(project: Project) {}
        override suspend fun isProjectNameTaken(name: String): Boolean = false
        override suspend fun deleteProject(id: Long) {}
        override fun getTransactionsFlow(): Flow<List<Transaction>> = MutableStateFlow(emptyList())
        override fun getTransactionsByAccountFlow(accountId: Long): Flow<List<Transaction>> = MutableStateFlow(emptyList())
        override suspend fun saveTransaction(transaction: Transaction): Long = 0
        override suspend fun saveTransactions(transactions: List<Transaction>): List<Long> {
            savedTransactions.addAll(transactions)
            return transactions.map { 0L }
        }
        override suspend fun deleteTransaction(id: Long) {}
    }
}
