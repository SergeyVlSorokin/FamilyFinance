package com.familyfinance.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.familyfinance.data.local.dao.FinanceDao
import com.familyfinance.data.local.entity.AccountEntity
import com.familyfinance.data.local.entity.AccountType
import com.familyfinance.data.local.entity.CategoryEntity
import com.familyfinance.data.local.entity.CategoryType
import com.familyfinance.data.local.entity.ProjectEntity
import com.familyfinance.data.local.entity.TransactionEntity
import com.familyfinance.data.local.entity.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class FinanceDatabaseUnitTest {

    private lateinit var db: FamilyFinanceDatabase
    private lateinit var dao: FinanceDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, FamilyFinanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAccountAndReadInList() = runBlocking {
        val account = AccountEntity(
            name = "Test Account",
            type = AccountType.BANK,
            currency = "USD",
            color = 0xFF00FF00.toInt()
        )
        dao.upsertAccount(account)
        val accounts = dao.getAccountsFlow().first()
        assertEquals("Test Account", accounts[0].name)
    }

    @Test
    fun insertAndReadTransaction() = runBlocking {
        val account = AccountEntity(
            name = "Main Wallet",
            type = AccountType.CASH,
            currency = "USD",
            color = 0xFF00FF00.toInt()
        )
        val accountId = dao.upsertAccount(account)

        val transaction = TransactionEntity(
            date = System.currentTimeMillis(),
            amountCents = -200,
            accountId = accountId,
            categoryId = null,
            projectId = null,
            note = "Coffee",
            type = TransactionType.EXPENSE,
            currencyCode = "USD"
        )
        
        dao.insertTransaction(transaction)

        val transactions = dao.getTransactionsByAccountFlow(accountId).first()
        assertEquals(1, transactions.size)
        assertEquals(-200L, transactions[0].amountCents)
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun duplicateAccountNameThrowsException() = runBlocking<Unit> {
        val account1 = AccountEntity(name = "Dup", type = AccountType.BANK, currency = "USD", color = 0)
        val account2 = AccountEntity(name = "Dup", type = AccountType.BANK, currency = "USD", color = 0)
        dao.upsertAccount(account1)
        dao.upsertAccount(account2)
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun duplicateCategoryNameThrowsException() = runBlocking<Unit> {
        val cat1 = CategoryEntity(name = "Dup", type = CategoryType.EXPENSE, icon = "", color = 0)
        val cat2 = CategoryEntity(name = "Dup", type = CategoryType.EXPENSE, icon = "", color = 0)
        dao.upsertCategory(cat1)
        dao.upsertCategory(cat2)
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun duplicateProjectNameThrowsException() = runBlocking<Unit> {
        val proj1 = ProjectEntity(name = "Dup", color = 0)
        val proj2 = ProjectEntity(name = "Dup", color = 0)
        dao.upsertProject(proj1)
        dao.upsertProject(proj2)
    }

    @Test
    fun transactionContainsCurrencyFields() = runBlocking {
        val account = AccountEntity(name = "Wallet", type = AccountType.CASH, currency = "EUR", color = 0)
        val accountId = dao.upsertAccount(account)

        val transaction = TransactionEntity(
            date = System.currentTimeMillis(),
            amountCents = 1000,
            accountId = accountId,
            categoryId = null,
            projectId = null,
            note = "Multi-currency test",
            type = TransactionType.EXPENSE,
            currencyCode = "EUR", // This will fail compilation initially
            targetAmountCents = 1100L
        )
        dao.insertTransaction(transaction)

        val saved = dao.getTransactionsByAccountFlow(accountId).first()[0]
        assertEquals("EUR", saved.currencyCode)
        assertEquals(1100L, saved.targetAmountCents)
    }
}
