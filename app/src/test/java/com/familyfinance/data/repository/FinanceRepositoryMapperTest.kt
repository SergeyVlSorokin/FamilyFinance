package com.familyfinance.data.repository

import com.familyfinance.data.local.entity.TransactionEntity
import com.familyfinance.data.local.entity.TransactionType as EntityType
import com.familyfinance.data.local.toDomain
import com.familyfinance.data.local.toEntity
import com.familyfinance.domain.model.Transaction
import com.familyfinance.domain.model.TransactionType as DomainType
import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceRepositoryMapperTest {

    @Test
    fun transactionEntityToDomainMapsCurrencyFields() {
        val entity = TransactionEntity(
            id = 100,
            date = 123456789,
            amountCents = 1000,
            accountId = 1,
            categoryId = 2,
            projectId = 3,
            note = "Test Note",
            type = EntityType.EXPENSE,
            currencyCode = "EUR",
            targetAmountCents = 1100L
        )

        val domain = entity.toDomain()

        assertEquals("EUR", domain.currencyCode)
        assertEquals(1100L, domain.targetAmountCents)
    }

    @Test
    fun transactionDomainToEntityMapsCurrencyFields() {
        val domain = Transaction(
            id = 100,
            date = 123456789,
            amountCents = 1000,
            accountId = 1,
            categoryId = 2,
            projectId = 3,
            note = "Test Note",
            type = DomainType.EXPENSE,
            currencyCode = "GBP",
            targetAmountCents = 1500L
        )

        val entity = domain.toEntity()

        assertEquals("GBP", entity.currencyCode)
        assertEquals(1500L, entity.targetAmountCents)
    }
}
