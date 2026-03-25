package com.familyfinance.data.repository

import com.familyfinance.data.local.dao.FinanceDao
import com.familyfinance.data.local.entity.*
import com.familyfinance.domain.model.*
import com.familyfinance.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepositoryImpl @Inject constructor(
    private val dao: FinanceDao
) : FinanceRepository {

    override fun getAccountsFlow(): Flow<List<Account>> =
        dao.getAccountsFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun getAccountById(id: Long): Account? =
        dao.getAccountById(id)?.toDomain()

    override suspend fun saveAccount(account: Account): Long =
        dao.upsertAccount(account.toEntity())

    override suspend fun updateAccountReconciliationDate(accountId: Long, timestamp: Long) =
        dao.updateReconciliationTimestamp(accountId, timestamp)

    override suspend fun isAccountNameTaken(name: String): Boolean =
        dao.isAccountNameTaken(name)

    override suspend fun deleteAccount(id: Long) =
        dao.deleteAccount(id)

    override fun getCategoriesFlow(): Flow<List<Category>> =
        dao.getCategoriesFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun saveCategory(category: Category) =
        dao.upsertCategory(category.toEntity())

    override suspend fun isCategoryNameTaken(name: String): Boolean =
        dao.isCategoryNameTaken(name)

    override suspend fun deleteCategory(id: Long) =
        dao.deleteCategory(id)

    override fun getProjectsFlow(): Flow<List<Project>> =
        dao.getProjectsFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun saveProject(project: Project) =
        dao.upsertProject(project.toEntity())

    override suspend fun isProjectNameTaken(name: String): Boolean =
        dao.isProjectNameTaken(name)

    override suspend fun deleteProject(id: Long) =
        dao.deleteProject(id)

    override fun getTransactionsFlow(): Flow<List<Transaction>> =
        dao.getTransactionsFlow().map { list -> list.map { it.toDomain() } }

    override fun getTransactionsByAccountFlow(accountId: Long): Flow<List<Transaction>> =
        dao.getTransactionsByAccountFlow(accountId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveTransaction(transaction: Transaction): Long =
        dao.insertTransaction(transaction.toEntity())

    override suspend fun saveTransactions(transactions: List<Transaction>): List<Long> =
        dao.insertTransactions(transactions.map { it.toEntity() })

    override suspend fun deleteTransaction(id: Long) =
        dao.deleteTransaction(id)

    // Mappers
    private fun AccountEntity.toDomain() = Account(
        id = id,
        name = name,
        type = com.familyfinance.domain.model.AccountType.valueOf(type.name),
        currency = currency,
        color = color,
        ownerLabel = ownerLabel,
        lastReconciledAt = lastReconciledAt
    )

    private fun Account.toEntity() = AccountEntity(
        id = id,
        name = name,
        type = com.familyfinance.data.local.entity.AccountType.valueOf(type.name),
        currency = currency,
        color = color,
        ownerLabel = ownerLabel,
        lastReconciledAt = lastReconciledAt
    )

    private fun CategoryEntity.toDomain() = Category(
        id = id,
        name = name,
        type = com.familyfinance.domain.model.CategoryType.valueOf(type.name),
        icon = icon,
        color = color
    )

    private fun Category.toEntity() = CategoryEntity(
        id = id,
        name = name,
        type = com.familyfinance.data.local.entity.CategoryType.valueOf(type.name),
        icon = icon,
        color = color
    )

    private fun ProjectEntity.toDomain() = Project(
        id = id,
        name = name,
        color = color,
        startDate = startDate,
        endDate = endDate
    )

    private fun Project.toEntity() = ProjectEntity(
        id = id,
        name = name,
        color = color,
        startDate = startDate,
        endDate = endDate
    )

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        date = date,
        amountCents = amountCents,
        accountId = accountId,
        categoryId = categoryId,
        projectId = projectId,
        note = note,
        type = com.familyfinance.domain.model.TransactionType.valueOf(type.name),
        targetAccountId = targetAccountId,
        receiptGroupId = receiptGroupId,
        transferLinkedId = transferLinkedId
    )

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        date = date,
        amountCents = amountCents,
        accountId = accountId,
        categoryId = categoryId,
        projectId = projectId,
        note = note,
        type = com.familyfinance.data.local.entity.TransactionType.valueOf(type.name),
        targetAccountId = targetAccountId,
        receiptGroupId = receiptGroupId,
        transferLinkedId = transferLinkedId
    )
}
