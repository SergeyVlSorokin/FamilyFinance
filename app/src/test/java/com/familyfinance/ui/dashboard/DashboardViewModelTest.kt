package com.familyfinance.ui.dashboard

import com.familyfinance.domain.model.Account
import com.familyfinance.domain.model.AccountType
import com.familyfinance.domain.usecase.AccountBalance
import com.familyfinance.domain.usecase.GetAccountBalancesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getAccountBalancesUseCase: GetAccountBalancesUseCase = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when balances are provided then state reflects wealth sum`() = runTest {
        // Given
        val balances = listOf(
            AccountBalance(Account(id = 1, name = "Cash", type = AccountType.CASH, currency = "USD", color = 0), 1000L),
            AccountBalance(Account(id = 2, name = "Bank", type = AccountType.BANK, currency = "USD", color = 0), 5500L)
        )
        whenever(getAccountBalancesUseCase()).thenReturn(flowOf(balances))

        // When
        val viewModel = DashboardViewModel(getAccountBalancesUseCase)
        
        // Then
        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(2, state.accounts.size)
        assertEquals(6500L, state.totalWealth["USD"])
        assertFalse(state.isLoading)
    }

    @Test
    fun `when multi-currency balances are provided then state reflects per-currency wealth`() = runTest {
        // Given
        val balances = listOf(
            AccountBalance(Account(id = 1, name = "Cash USD", type = AccountType.CASH, currency = "USD", color = 0), 1000L),
            AccountBalance(Account(id = 2, name = "Cash SEK", type = AccountType.CASH, currency = "SEK", color = 0), 5000L)
        )
        whenever(getAccountBalancesUseCase()).thenReturn(flowOf(balances))

        // When
        val viewModel = DashboardViewModel(getAccountBalancesUseCase)
        
        // Then
        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(2, state.accounts.size)
        assertEquals(1000L, state.totalWealth["USD"])
        assertEquals(5000L, state.totalWealth["SEK"])
    }

    @Test
    fun `when no accounts then wealth is empty and accounts list empty`() = runTest {
        // Given
        whenever(getAccountBalancesUseCase()).thenReturn(flowOf(emptyList()))

        // When
        val viewModel = DashboardViewModel(getAccountBalancesUseCase)
        
        // Then
        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(0, state.accounts.size)
        assertEquals(0, state.totalWealth.size)
        assertFalse(state.isLoading)
    }
}
