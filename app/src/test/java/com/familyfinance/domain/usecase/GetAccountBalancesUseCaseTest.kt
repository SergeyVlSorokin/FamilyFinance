package com.familyfinance.domain.usecase

import com.familyfinance.domain.model.*
import com.familyfinance.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// @trace TASK-113

class GetAccountBalancesUseCaseTest {

    private lateinit var repository: FakeFinanceRepository
    private lateinit var useCase: GetAccountBalancesUseCase

    @Before
    fun setUp() {
        repository = FakeFinanceRepository()
        useCase = GetAccountBalancesUseCase(repository)
    }

    @Test
    fun `compute balances with various transaction types`() = runBlocking {
        // Given
        val account1 = Account(id = 1, name = "Cash", type = AccountType.CASH, currency = "USD", color = 0)
        val account2 = Account(id = 2, name = "Bank", type = AccountType.BANK, currency = "USD", color = 0)
        repository.accounts.value = listOf(account1, account2)

        repository.transactions.value = listOf(
            Transaction(id = 1, date = 0, amountCents = 1000, accountId = 1, categoryId = null, projectId = null, note = "Opening", type = TransactionType.OPENING_BALANCE, currencyCode = "USD"),
            Transaction(id = 2, date = 1, amountCents = 500, accountId = 1, categoryId = 1, projectId = null, note = "Income", type = TransactionType.INCOME, currencyCode = "USD"),
            Transaction(id = 3, date = 2, amountCents = 200, accountId = 1, categoryId = 2, projectId = null, note = "Expense", type = TransactionType.EXPENSE, currencyCode = "USD"),
            Transaction(id = 4, date = 3, amountCents = 300, accountId = 1, targetAccountId = 2, categoryId = null, projectId = null, note = "Transfer Out", type = TransactionType.TRANSFER, currencyCode = "USD")
        )

        // When
        val balances = useCase().first()

        // Then
        val bal1 = balances.find { it.account.id == 1L }?.balanceCents
        val bal2 = balances.find { it.account.id == 2L }?.balanceCents

        // 1000 + 500 - 200 - 300 = 1000
        assertEquals(1000L, bal1)
        // 0 + 300 = 300
        assertEquals(300L, bal2)
    }

    @Test
    fun `compute balances up to specific date`() = runBlocking {
        // Given
        val account = Account(id = 1, name = "Cash", type = AccountType.CASH, currency = "USD", color = 0)
        repository.accounts.value = listOf(account)
        repository.transactions.value = listOf(
            Transaction(id = 1, date = 1000, amountCents = 1000, accountId = 1, categoryId = null, projectId = null, note = "Opening", type = TransactionType.OPENING_BALANCE, currencyCode = "USD"),
            Transaction(id = 2, date = 2000, amountCents = 500, accountId = 1, categoryId = 1, projectId = null, note = "Income", type = TransactionType.INCOME, currencyCode = "USD"),
            Transaction(id = 3, date = 3000, amountCents = 200, accountId = 1, categoryId = 2, projectId = null, note = "Expense", type = TransactionType.EXPENSE, currencyCode = "USD")
        )

        // When - calculate up to date 2000
        val balances = useCase(2000L).first()

        // Then - should only include transactions 1 and 2 (1000 + 500 = 1500)
        val bal = balances.find { it.account.id == 1L }?.balanceCents
        assertEquals(1500L, bal)
    }

    private class FakeFinanceRepository : FinanceRepository {
        val accounts = MutableStateFlow<List<Account>>(emptyList())
        val transactions = MutableStateFlow<List<Transaction>>(emptyList())

        override fun getAccountsFlow(): Flow<List<Account>> = accounts
        override suspend fun getAccountById(id: Long): Account? = accounts.value.find { it.id == id }
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
        override fun getTransactionsFlow(): Flow<List<Transaction>> = transactions
        override fun getTransactionsByAccountFlow(accountId: Long): Flow<List<Transaction>> = MutableStateFlow(emptyList())
        override suspend fun saveTransaction(transaction: Transaction): Long = 0
        override suspend fun saveTransactions(transactions: List<Transaction>): List<Long> = emptyList()
        override suspend fun deleteTransaction(id: Long) {}
    }
}
