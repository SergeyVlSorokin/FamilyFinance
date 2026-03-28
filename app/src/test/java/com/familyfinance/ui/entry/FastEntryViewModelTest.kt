package com.familyfinance.ui.entry

import com.familyfinance.domain.model.*
import com.familyfinance.domain.repository.FinanceRepository
import com.familyfinance.domain.usecase.SaveSplitReceiptUseCase
import com.familyfinance.domain.usecase.SaveTransactionUseCase
import com.familyfinance.domain.usecase.SaveTransferUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class FastEntryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: FinanceRepository = mock()
    private val saveTransactionUseCase: SaveTransactionUseCase = mock()
    private val saveSplitReceiptUseCase: SaveSplitReceiptUseCase = mock()
    private val saveTransferUseCase: SaveTransferUseCase = mock()

    private val accounts = listOf(
        Account(id = 1, name = "Bank USD", type = AccountType.BANK, currency = "USD", color = 0),
        Account(id = 2, name = "Cash SEK", type = AccountType.CASH, currency = "SEK", color = 0),
        Account(id = 3, name = "Visa USD", type = AccountType.CREDIT_CARD, currency = "USD", color = 0)
    )
    private val categories = listOf(
        Category(id = 1, name = "Food", type = CategoryType.EXPENSE, icon = "", color = 0)
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
    fun `initial state loads accounts and categories`() = runTest {
        val viewModel = FastEntryViewModel(repository, saveTransactionUseCase, saveSplitReceiptUseCase, saveTransferUseCase)
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.accounts.size)
        assertEquals(1, state.categories.size)
    }

    @Test
    fun `remainder updates when splits are added`() = runTest {
        val viewModel = FastEntryViewModel(repository, saveTransactionUseCase, saveSplitReceiptUseCase, saveTransferUseCase)
        viewModel.onAmountChange(10000L) // 100.00
        viewModel.addSplit()
        viewModel.updateSplit(0, SplitLine(categories[0], 8000L)) // 80.00
        
        assertEquals(2000L, viewModel.uiState.value.remainderCents)
    }

    @Test
    fun `isValid is true for valid single expense`() = runTest {
        val viewModel = FastEntryViewModel(repository, saveTransactionUseCase, saveSplitReceiptUseCase, saveTransferUseCase)
        viewModel.onAmountChange(5000L)
        viewModel.onAccountChange(accounts[0])
        viewModel.onCategoryChange(categories[0])
        
        assertTrue(viewModel.uiState.value.isValid)
    }

    @Test
    fun `isValid is false for transfer to same account`() = runTest {
        val viewModel = FastEntryViewModel(repository, saveTransactionUseCase, saveSplitReceiptUseCase, saveTransferUseCase)
        viewModel.onTypeChange(TransactionType.TRANSFER)
        viewModel.onAmountChange(5000L)
        viewModel.onAccountChange(accounts[0])
        viewModel.onTargetAccountChange(accounts[0])
        
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `save calls saveTransactionUseCase for single expense`() = runTest {
        whenever(saveTransactionUseCase.invoke(any())).thenReturn(Result.success(1L))
        val viewModel = FastEntryViewModel(repository, saveTransactionUseCase, saveSplitReceiptUseCase, saveTransferUseCase)
        
        viewModel.onAmountChange(5000L)
        viewModel.onAccountChange(accounts[0])
        viewModel.onCategoryChange(categories[0])
        viewModel.save()
        
        verify(saveTransactionUseCase).invoke(any())
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `save calls saveSplitReceiptUseCase when splits exist`() = runTest {
        whenever(saveSplitReceiptUseCase.invoke(any(), any())).thenReturn(Result.success(listOf(1L, 2L)))
        val viewModel = FastEntryViewModel(repository, saveTransactionUseCase, saveSplitReceiptUseCase, saveTransferUseCase)
        
        viewModel.onAmountChange(10000L)
        viewModel.onAccountChange(accounts[0])
        viewModel.addSplit()
        viewModel.updateSplit(0, SplitLine(categories[0], 10000L))
        viewModel.save()
        
        verify(saveSplitReceiptUseCase).invoke(eq(10000L), any())
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `save calls saveTransferUseCase for transfer`() = runTest {
        whenever(saveTransferUseCase.invoke(any(), any(), any(), any(), any(), anyOrNull())).thenReturn(Result.success(1L))
        val viewModel = FastEntryViewModel(repository, saveTransactionUseCase, saveSplitReceiptUseCase, saveTransferUseCase)
        
        viewModel.onTypeChange(TransactionType.TRANSFER)
        viewModel.onAmountChange(5000L)
        viewModel.onAccountChange(accounts[0])
        viewModel.onTargetAccountChange(accounts[2])
        viewModel.save()
        
        verify(saveTransferUseCase).invoke(any(), eq(5000L), eq(accounts[0]), eq(accounts[2]), any(), anyOrNull())
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `isFxTransfer is true when accounts have different currencies`() = runTest {
        val viewModel = FastEntryViewModel(repository, saveTransactionUseCase, saveSplitReceiptUseCase, saveTransferUseCase)
        viewModel.onTypeChange(TransactionType.TRANSFER)
        viewModel.onAccountChange(accounts[0]) // USD
        viewModel.onTargetAccountChange(accounts[1]) // SEK
        
        assertTrue(viewModel.uiState.value.isFxTransfer)
    }

    @Test
    fun `isValid is false for FX transfer with zero target amount`() = runTest {
        val viewModel = FastEntryViewModel(repository, saveTransactionUseCase, saveSplitReceiptUseCase, saveTransferUseCase)
        viewModel.onTypeChange(TransactionType.TRANSFER)
        viewModel.onAmountChange(5000L)
        viewModel.onAccountChange(accounts[0]) // USD
        viewModel.onTargetAccountChange(accounts[1]) // SEK
        
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `save passes targetAmountCents for FX transfer`() = runTest {
        whenever(saveTransferUseCase.invoke(any(), any(), any(), any(), any(), anyOrNull())).thenReturn(Result.success(1L))
        val viewModel = FastEntryViewModel(repository, saveTransactionUseCase, saveSplitReceiptUseCase, saveTransferUseCase)
        
        viewModel.onTypeChange(TransactionType.TRANSFER)
        viewModel.onAmountChange(5000L) // 50.00 USD
        viewModel.onAccountChange(accounts[0]) // USD
        viewModel.onTargetAccountChange(accounts[1]) // SEK
        viewModel.onTargetAmountChange(55000L) // 550.00 SEK
        viewModel.save()
        
        verify(saveTransferUseCase).invoke(any(), eq(5000L), eq(accounts[0]), eq(accounts[1]), any(), eq(55000L))
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `onDateChange updates state and save uses it`() = runTest {
        whenever(saveTransactionUseCase.invoke(any())).thenReturn(Result.success(1L))
        val viewModel = FastEntryViewModel(repository, saveTransactionUseCase, saveSplitReceiptUseCase, saveTransferUseCase)
        val newDate = 987654321L
        
        viewModel.onDateChange(newDate)
        viewModel.onAmountChange(5000L)
        viewModel.onAccountChange(accounts[0])
        viewModel.onCategoryChange(categories[0])
        viewModel.save()
        
        val transactionCaptor = argumentCaptor<Transaction>()
        verify(saveTransactionUseCase).invoke(transactionCaptor.capture())
        assertEquals(newDate, transactionCaptor.firstValue.date)
    }
}
