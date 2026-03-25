package com.familyfinance.ui.settings

import com.familyfinance.domain.model.Account
import com.familyfinance.domain.model.AccountType
import com.familyfinance.domain.model.Category
import com.familyfinance.domain.model.CategoryType
import com.familyfinance.domain.model.Project
import com.familyfinance.domain.repository.FinanceRepository
import com.familyfinance.domain.usecase.CreateAccountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: FinanceRepository = mock()
    private val createAccountUseCase: CreateAccountUseCase = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(repository.getAccountsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.getCategoriesFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.getProjectsFlow()).thenReturn(flowOf(emptyList()))
        
        runBlocking {
            whenever(repository.isAccountNameTaken(any())).thenReturn(false)
            whenever(repository.isCategoryNameTaken(any())).thenReturn(false)
            whenever(repository.isProjectNameTaken(any())).thenReturn(false)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when data is provided then state reflects all lists`() = runTest {
        // Given
        val accounts = listOf(Account(name = "Test Account", type = AccountType.BANK, currency = "USD", color = 0))
        val categories = listOf(Category(name = "Food", type = CategoryType.EXPENSE, icon = "fastfood", color = 0))
        val projects = listOf(Project(name = "Home", color = 0))
        
        whenever(repository.getAccountsFlow()).thenReturn(flowOf(accounts))
        whenever(repository.getCategoriesFlow()).thenReturn(flowOf(categories))
        whenever(repository.getProjectsFlow()).thenReturn(flowOf(projects))

        // When
        val viewModel = SettingsViewModel(repository, createAccountUseCase)
        
        // Then
        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(1, state.accounts.size)
        assertEquals(1, state.categories.size)
        assertEquals(1, state.projects.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `saveAccount calls CreateAccountUseCase with timestamp`() = runTest {
        val viewModel = SettingsViewModel(repository, createAccountUseCase)
        val account = Account(name = "New", type = AccountType.CASH, currency = "USD", color = 0)
        val timestamp = 123456789L
        
        viewModel.saveAccount(account, 1000L, timestamp)
        
        verify(createAccountUseCase).invoke(eq(account), eq(1000L), eq(timestamp))
    }

    @Test
    fun `saveCategory calls repository`() = runTest {
        val viewModel = SettingsViewModel(repository, createAccountUseCase)
        val category = Category(name = "Misc", type = CategoryType.EXPENSE, icon = "help", color = 0)
        
        viewModel.saveCategory(category)
        
        verify(repository).saveCategory(eq(category))
    }

    @Test
    fun `saveProject calls repository`() = runTest {
        val viewModel = SettingsViewModel(repository, createAccountUseCase)
        val project = Project(name = "Vacation", color = 0)
        
        viewModel.saveProject(project)
        
        verify(repository).saveProject(eq(project))
    }

    @Test
    fun `saveCategory with existing name sets error`() = runTest {
        val viewModel = SettingsViewModel(repository, createAccountUseCase)
        
        // Start collecting uiState to trigger combine
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        val category = Category(name = "Food", type = CategoryType.EXPENSE, icon = "", color = 0)
        whenever(repository.isCategoryNameTaken("Food")).thenReturn(true)
        
        viewModel.saveCategory(category)
        
        testScheduler.runCurrent()
        
        assertEquals("Category name already taken", viewModel.uiState.value.error)
    }
}
