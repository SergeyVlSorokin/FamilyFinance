package com.familyfinance.data.local

import com.familyfinance.data.local.entity.*
import com.familyfinance.domain.model.*

internal fun AccountEntity.toDomain() = Account(
    id = id,
    name = name,
    type = com.familyfinance.domain.model.AccountType.valueOf(type.name),
    currency = currency,
    color = color,
    ownerLabel = ownerLabel,
    lastReconciledAt = lastReconciledAt
)

internal fun Account.toEntity() = AccountEntity(
    id = id,
    name = name,
    type = com.familyfinance.data.local.entity.AccountType.valueOf(type.name),
    currency = currency,
    color = color,
    ownerLabel = ownerLabel,
    lastReconciledAt = lastReconciledAt
)

internal fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    type = com.familyfinance.domain.model.CategoryType.valueOf(type.name),
    icon = icon,
    color = color
)

internal fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    type = com.familyfinance.data.local.entity.CategoryType.valueOf(type.name),
    icon = icon,
    color = color
)

internal fun ProjectEntity.toDomain() = Project(
    id = id,
    name = name,
    color = color,
    startDate = startDate,
    endDate = endDate
)

internal fun Project.toEntity() = ProjectEntity(
    id = id,
    name = name,
    color = color,
    startDate = startDate,
    endDate = endDate
)

internal fun TransactionEntity.toDomain() = Transaction(
    id = id,
    date = date,
    amountCents = amountCents,
    accountId = accountId,
    categoryId = categoryId,
    projectId = projectId,
    note = note,
    type = com.familyfinance.domain.model.TransactionType.valueOf(type.name),
    currencyCode = currencyCode,
    targetAccountId = targetAccountId,
    targetAmountCents = targetAmountCents,
    receiptGroupId = receiptGroupId,
    transferLinkedId = transferLinkedId
)

/** @trace TASK-115 */
internal fun Transaction.toEntity() = TransactionEntity(
    id = id,
    date = date,
    amountCents = amountCents,
    accountId = accountId,
    categoryId = categoryId,
    projectId = projectId,
    note = note,
    type = com.familyfinance.data.local.entity.TransactionType.valueOf(type.name),
    currencyCode = currencyCode,
    targetAccountId = targetAccountId,
    targetAmountCents = targetAmountCents,
    receiptGroupId = receiptGroupId,
    transferLinkedId = transferLinkedId
)
