package com.familyfinance.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.familyfinance.domain.model.Transaction
import com.familyfinance.ui.components.formatAsCurrency
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.familyfinance.domain.model.AccountType
import com.familyfinance.domain.usecase.AccountBalance
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToFastEntry: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToReconcile: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var transactionIdToDismiss by remember { mutableStateOf<Long?>(null) } // @trace TASK-203

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Finance", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToTimeline) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Timeline"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onNavigateToFastEntry,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Header - Total Wealth
            Text(
                text = "Total Wealth",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            uiState.totalWealth.forEach { (currencyCode, amountCents) ->
                Text(
                    text = amountCents.formatAsCurrency(currencyCode),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (uiState.pendingReturns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                PendingReturnsWidget(
                    returns = uiState.pendingReturns,
                    onDismiss = { id -> transactionIdToDismiss = id }, // @trace TASK-203
                    onLogRefund = viewModel::initiateRefund
                )
            }

            uiState.refundContext?.let { context ->
                RefundEntryDialog(
                    refundContext = context,
                    onDismiss = viewModel::cancelRefund,
                    onConfirm = { amount, more ->
                        viewModel.confirmRefund(amount, more)
                    }
                )
            }

            if (transactionIdToDismiss != null) {
                AlertDialog(
                    onDismissRequest = { transactionIdToDismiss = null },
                    title = { Text("Dismiss Return Tracking") },
                    text = { Text("Are you sure you want to stop tracking the return for this transaction? This won't delete the transaction itself.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                transactionIdToDismiss?.let { viewModel.dismissReturn(it) }
                                transactionIdToDismiss = null
                            }
                        ) {
                            Text("Dismiss")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { transactionIdToDismiss = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Accounts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.accounts.isEmpty() && !uiState.isLoading) {
                EmptyState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(uiState.accounts) { accountBalance ->
                        AccountCard(
                            accountBalance = accountBalance,
                            onReconcile = { onNavigateToReconcile(accountBalance.account.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCard(
    accountBalance: AccountBalance,
    onReconcile: () -> Unit
) {
    Card(
        onClick = onReconcile,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp),
                onClick = onReconcile
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getIconForAccountType(accountBalance.account.type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = accountBalance.account.displayNameWithOwner,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = accountBalance.account.type.name.lowercase().capitalize(Locale.ROOT),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Text(
                text = accountBalance.balanceCents.formatAsCurrency(accountBalance.account.currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No accounts yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = "Add your first account in Settings",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}



fun getIconForAccountType(type: AccountType): ImageVector {
    return when (type) {
        AccountType.CASH -> Icons.Default.AccountBalanceWallet
        AccountType.BANK -> Icons.Default.CreditCard
        AccountType.CREDIT_CARD -> Icons.Default.CreditCard
        AccountType.INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
    }
}

@Composable
fun PendingReturnsWidget(
    returns: List<Transaction>,
    onDismiss: (Long) -> Unit,
    onLogRefund: (Transaction) -> Unit
) {
    Column {
        Text(
            text = "Pending Returns",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val displayReturns = returns.take(3)
                for (index in displayReturns.indices) {
                    val transaction = displayReturns[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = transaction.note.ifEmpty { "Transaction" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                text = transaction.amountCents.formatAsCurrency(transaction.currencyCode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(onClick = { onDismiss(transaction.id) }) {
                            Icon(
                                imageVector = Icons.Default.Close, 
                                contentDescription = "Stop tracking return", 
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(onClick = { onLogRefund(transaction) }) {
                            Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = "Log Refund", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (index < displayReturns.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                        )
                    }
                }
                if (returns.size > 3) {
                    Text(
                        text = "+ ${returns.size - 3} more",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

// Extension to capitalizing first letter comfortably
private fun String.capitalize(locale: Locale): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
