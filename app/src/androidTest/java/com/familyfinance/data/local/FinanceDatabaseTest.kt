package com.familyfinance.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.familyfinance.data.local.dao.FinanceDao
import com.familyfinance.data.local.entity.AccountEntity
import com.familyfinance.data.local.entity.AccountType
import com.familyfinance.data.local.entity.CategoryEntity
import com.familyfinance.data.local.entity.CategoryType
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
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class FinanceDatabaseTest {

    private lateinit var db: FamilyFinanceDatabase
    private lateinit var dao: FinanceDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
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
    @Throws(Exception::class)
    fun writeAccountAndReadInList() = runBlocking {
        val account = AccountEntity(
            name = "Test Account",
            type = AccountType.BANK,
            currency = "USD",
            currentBalanceCents = 10000,
            color = 0xFF00FF00.toInt()
        )
        dao.upsertAccount(account)
        val accounts = dao.getAccountsFlow().first()
        assertEquals(accounts[0].name, "Test Account")
    }

    @Test
    @Throws(Exception::class)
    fun insertTransactionUpdatesBalance() = runBlocking {
        val account = AccountEntity(
            name = "Main Wallet",
            type = AccountType.CASH,
            currency = "USD",
            currentBalanceCents = 1000,
            color = 0xFF00FF00.toInt()
        )
        val accountId = dao.upsertAccount(account)

        val transaction = TransactionEntity(
            date = System.currentTimeMillis(),
            amountCents = -200, // Expense
            accountId = accountId,
            categoryId = null,
            projectId = null,
            note = "Coffee",
            type = TransactionType.EXPENSE
        )
        
        dao.insertTransactionAndUpdateBalance(transaction)

        val updatedAccount = dao.getAccountById(accountId)
        assertNotNull(updatedAccount)
        assertEquals(800, updatedAccount?.currentBalanceCents)
    }
}
