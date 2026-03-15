package com.familyfinance.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.familyfinance.data.local.dao.FinanceDao
import com.familyfinance.data.local.entity.AccountEntity
import com.familyfinance.data.local.entity.AccountType
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
            type = TransactionType.EXPENSE
        )
        
        dao.insertTransaction(transaction)

        val transactions = dao.getTransactionsByAccountFlow(accountId).first()
        assertEquals(1, transactions.size)
        assertEquals(-200L, transactions[0].amountCents)
    }
}
