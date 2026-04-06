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

class SaveTransferUseCaseTest {

    private lateinit var repository: FakeFinanceRepository
    private lateinit var useCase: SaveTransferUseCase

    @Before
    fun setUp() {
        repository = FakeFinanceRepository()
        useCase = SaveTransferUseCase(repository)
    }

    @Test
    fun `same currency transfer succeeds without target amount`() = runBlocking {
        // Given
        val fromAccount = Account(id = 1, name = "From", type = AccountType.BANK, currency = "USD", color = 0)
        val toAccount = Account(id = 2, name = "To", type = AccountType.CASH, currency = "USD", color = 0)

        // When
        val result = useCase(
            date = 1000L,
            amountCents = 5000L,
            fromAccount = fromAccount,
            toAccount = toAccount,
            note = "Test",
            targetAmountCents = null
        )

        // Then
        assertTrue(result.isSuccess)
        val saved = repository.savedTransactions[0]
        assertEquals("USD", saved.currencyCode)
        assertEquals(null, saved.targetAmountCents)
    }

    @Test
    fun `FX transfer fails if target amount is missing`() = runBlocking {
        // Given
        val fromAccount = Account(id = 1, name = "From", type = AccountType.BANK, currency = "USD", color = 0)
        val toAccount = Account(id = 2, name = "To", type = AccountType.BANK, currency = "EUR", color = 0)

        // When
        val result = useCase(
            date = 1000L,
            amountCents = 5000L,
            fromAccount = fromAccount,
            toAccount = toAccount,
            note = "Test",
            targetAmountCents = null
        )

        // Then
        assertTrue(result.isFailure)
        assertEquals("Target amount is required for cross-currency transfers", result.exceptionOrNull()?.message)
    }

    @Test
    fun `FX transfer succeeds if target amount is provided`() = runBlocking {
        // Given
        val fromAccount = Account(id = 1, name = "From", type = AccountType.BANK, currency = "USD", color = 0)
        val toAccount = Account(id = 2, name = "To", type = AccountType.BANK, currency = "EUR", color = 0)

        // When
        val result = useCase(
            date = 1000L,
            amountCents = 5000L,
            fromAccount = fromAccount,
            toAccount = toAccount,
            note = "Test",
            targetAmountCents = 4500L
        )

        // Then
        assertTrue(result.isSuccess)
        val saved = repository.savedTransactions[0]
        assertEquals("USD", saved.currencyCode)
        assertEquals(4500L, saved.targetAmountCents)
    }

    private class FakeFinanceRepository : FinanceRepository {
        val savedTransactions = mutableListOf<Transaction>()

        override fun getAccountsFlow(): Flow<List<Account>> = MutableStateFlow(emptyList())
        override suspend fun getAccountById(id: Long): Account? = null
        override suspend fun saveAccount(account: Account): Long = 0
        override suspend fun updateAccountReconciliationDate(accountId: Long, timestamp: Long) {}
        override suspend fun isAccountUnique(name: String, currency: String, ownerLabel: String?): Boolean = false
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
        override suspend fun saveTransaction(transaction: Transaction): Long {
            savedTransactions.add(transaction)
            return 1L
        }
        override suspend fun saveTransactions(transactions: List<Transaction>): List<Long> = emptyList()
        override suspend fun deleteTransaction(id: Long) {}
    }
}
