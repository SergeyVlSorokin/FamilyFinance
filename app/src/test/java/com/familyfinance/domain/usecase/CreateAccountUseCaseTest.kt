package com.familyfinance.domain.usecase

import com.familyfinance.domain.model.Account
import com.familyfinance.domain.model.AccountType
import com.familyfinance.domain.model.Transaction
import com.familyfinance.domain.model.TransactionType
import com.familyfinance.domain.repository.FinanceRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CreateAccountUseCaseTest {

    private val repository: FinanceRepository = mock()
    private val useCase = CreateAccountUseCase(repository)

    @Test
    fun `when opening balance is non-zero, it creates a transaction with provided timestamp`() = runTest {
        // Given
        val account = Account(name = "Test", type = AccountType.BANK, currency = "USD", color = 0)
        val openingBalance = 1000L
        val timestamp = 123456789L
        whenever(repository.saveAccount(any())).thenReturn(1L)

        // When
        useCase(account, openingBalance, timestamp)

        // Then
        val transactionCaptor = argumentCaptor<Transaction>()
        verify(repository).saveTransaction(transactionCaptor.capture())
        
        val capturedTransaction = transactionCaptor.firstValue
        assertEquals(timestamp, capturedTransaction.date)
        assertEquals(openingBalance, capturedTransaction.amountCents)
        assertEquals(TransactionType.OPENING_BALANCE, capturedTransaction.type)
    }

    @Test
    fun `when opening balance is zero, no transaction is created`() = runTest {
        // Given
        val account = Account(name = "Test", type = AccountType.BANK, currency = "USD", color = 0)
        whenever(repository.saveAccount(any())).thenReturn(1L)

        // When
        useCase(account, 0L, 123456789L)

        // Then
        verify(repository, org.mockito.kotlin.never()).saveTransaction(any())
    }
}
