package com.familyfinance.ui.timeline

import com.familyfinance.domain.model.*
import com.familyfinance.domain.repository.FinanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: FinanceRepository = mock()

    private val septDate = LocalDate.of(2023, 9, 15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val octDate = LocalDate.of(2023, 10, 5).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private val accounts = listOf(
        Account(id = 1, name = "Bank", type = AccountType.BANK, currency = "USD", color = 0, ownerLabel = "Serge"),
        Account(id = 2, name = "Cash", type = AccountType.CASH, currency = "USD", color = 0, ownerLabel = "Elena")
    )
    private val categories = listOf(
        Category(id = 1, name = "Food", type = CategoryType.EXPENSE, icon = "", color = 0),
        Category(id = 2, name = "Salary", type = CategoryType.INCOME, icon = "", color = 0)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(repository.getAccountsFlow()).thenReturn(flowOf(accounts))
        whenever(repository.getCategoriesFlow()).thenReturn(flowOf(categories))
        whenever(repository.getProjectsFlow()).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state groups transactions by month`() = runTest {
        val trans = listOf(
            Transaction(id = 1, date = septDate, amountCents = 1000, accountId = 1, categoryId = 1, projectId = null, note = "", type = TransactionType.EXPENSE),
            Transaction(id = 2, date = octDate, amountCents = 5000, accountId = 1, categoryId = 2, projectId = null, note = "", type = TransactionType.INCOME)
        )
        whenever(repository.getTransactionsFlow()).thenReturn(flowOf(trans))

        val viewModel = TimelineViewModel(repository)
        val state = viewModel.uiState.value
        
        assertEquals(2, state.groups.size)
        // Grouped by descending date, so October should be first
        assertEquals("October 2023", state.groups[0].monthYear)
        assertEquals("September 2023", state.groups[1].monthYear)
    }

    @Test
    fun `filter by owner works correctly`() = runTest {
        val trans = listOf(
            Transaction(id = 1, date = septDate, amountCents = 1000, accountId = 1, categoryId = 1, projectId = null, note = "", type = TransactionType.EXPENSE),
            Transaction(id = 2, date = septDate, amountCents = 2000, accountId = 2, categoryId = 1, projectId = null, note = "", type = TransactionType.EXPENSE)
        )
        whenever(repository.getTransactionsFlow()).thenReturn(flowOf(trans))

        val viewModel = TimelineViewModel(repository)
        viewModel.onOwnerFilterChange("Serge")
        
        val state = viewModel.uiState.value
        assertEquals(1, state.groups[0].items.size)
        assertEquals("Serge", state.groups[0].items[0].account?.ownerLabel)
    }

    @Test
    fun `split grouping identifies root and children`() = runTest {
        val groupId = "test-group"
        val trans = listOf(
            Transaction(id = 1, date = septDate, amountCents = 1000, accountId = 1, categoryId = 1, projectId = null, note = "Root", type = TransactionType.EXPENSE, receiptGroupId = groupId),
            Transaction(id = 2, date = septDate, amountCents = 500, accountId = 1, categoryId = 1, projectId = null, note = "Child", type = TransactionType.EXPENSE, receiptGroupId = groupId)
        )
        // Order in list matters for "firstInGroup" logic. 1 is root if it's the last one in filteredTransactions (which is sorted by date desc).
        // Actually findLast on sorted by date desc means the earliest transaction in that group on that date.
        whenever(repository.getTransactionsFlow()).thenReturn(flowOf(trans))

        val viewModel = TimelineViewModel(repository)
        val state = viewModel.uiState.value
        val items = state.groups[0].items
        
        // Root is the one with smallest ID or earliest? 
        // My logic: filteredTransactions.findLast { it.receiptGroupId == trans.receiptGroupId }
        // trans = [2, 1] because of sortedByDescending date (same date, so stable sort order depends on original).
        // If trans is [1, 2], findLast returns 2. So 2 is root? No, 1 should be root.
        // Let's check logic: val firstInGroup = filteredTransactions.findLast { it.receiptGroupId == trans.receiptGroupId }
        // If trans is [2, 1], findLast is 1. So 1 is root. Correct.
        
        val root = items.find { it.transaction.id == 1L }
        val child = items.find { it.transaction.id == 2L }
        
        assertTrue(root?.isSplitRoot == true)
        assertTrue(child?.isSplitChild == true)
    }

    @Test
    fun `special transaction types are marked as system entries`() = runTest {
        val trans = listOf(
            Transaction(id = 1, date = septDate, amountCents = 0, accountId = 1, categoryId = null, projectId = null, note = "", type = TransactionType.REVALUATION)
        )
        whenever(repository.getTransactionsFlow()).thenReturn(flowOf(trans))

        val viewModel = TimelineViewModel(repository)
        assertTrue(viewModel.uiState.value.groups[0].items[0].isSystemEntry)
    }
}
