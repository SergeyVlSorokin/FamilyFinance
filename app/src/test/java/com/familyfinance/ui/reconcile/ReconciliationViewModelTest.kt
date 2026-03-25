package com.familyfinance.ui.reconcile

import androidx.lifecycle.SavedStateHandle
import com.familyfinance.domain.model.Account
import com.familyfinance.domain.model.AccountType
import com.familyfinance.domain.usecase.AccountBalance
import com.familyfinance.domain.usecase.GetAccountBalancesUseCase
import com.familyfinance.domain.usecase.ReconcileAccountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.mockito.ArgumentMatchers.anyLong

// @trace TASK-113

@OptIn(ExperimentalCoroutinesApi::class)
class ReconciliationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getAccountBalancesUseCase: GetAccountBalancesUseCase = mock()
    private val reconcileAccountUseCase: ReconcileAccountUseCase = mock()
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf("accountId" to 1L))

    private val account = Account(id = 1, name = "Bank", type = AccountType.BANK, currency = "USD", color = 0)
    private val accountBalance = AccountBalance(account, 100000) // $1000.00

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(getAccountBalancesUseCase(anyOrNull())).thenReturn(flowOf(listOf(accountBalance)))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads correct account`() = runTest {
        val viewModel = ReconciliationViewModel(getAccountBalancesUseCase, reconcileAccountUseCase, savedStateHandle)
        val state = viewModel.uiState.value
        
        assertEquals(1L, state.accountBalance?.account?.id)
        assertEquals(100000L, state.accountBalance?.balanceCents)
    }

    @Test
    fun `discrepancy is computed correctly when input changes`() = runTest {
        val viewModel = ReconciliationViewModel(getAccountBalancesUseCase, reconcileAccountUseCase, savedStateHandle)
        
        viewModel.onActualBalanceChange("1050.00") // $1050.00
        
        val state = viewModel.uiState.value
        assertEquals(5000L, state.discrepancyCents) // $50.00 discrepancy
    }

    @Test
    fun `negative discrepancy is computed correctly`() = runTest {
        val viewModel = ReconciliationViewModel(getAccountBalancesUseCase, reconcileAccountUseCase, savedStateHandle)
        
        viewModel.onActualBalanceChange("970") // $970.00
        
        val state = viewModel.uiState.value
        assertEquals(-3000L, state.discrepancyCents) // -$30.00 discrepancy
    }

    @Test
    fun `reconcile calls usecase with correct parameters`() = runTest {
        whenever(reconcileAccountUseCase.invoke(any(), any(), any())).thenReturn(Result.success(1L))
        
        val viewModel = ReconciliationViewModel(getAccountBalancesUseCase, reconcileAccountUseCase, savedStateHandle)
        viewModel.onActualBalanceChange("1000.00")
        viewModel.reconcile()
        
        verify(reconcileAccountUseCase).invoke(
            eq(1L),
            eq(100000L),
            any()
        )
        
        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `invalid input handles gracefully`() = runTest {
        val viewModel = ReconciliationViewModel(getAccountBalancesUseCase, reconcileAccountUseCase, savedStateHandle)
        
        viewModel.onActualBalanceChange("abc")
        
        val state = viewModel.uiState.value
        assertEquals("", state.actualBalanceInput)
        assertNull(state.discrepancyCents)
    }
}
