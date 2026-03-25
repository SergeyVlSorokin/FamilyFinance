package com.familyfinance.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.familyfinance.domain.model.TransactionType
import com.familyfinance.ui.components.getIconByName
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timeline", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            FilterBar(
                state = uiState,
                onOwnerChange = viewModel::onOwnerFilterChange,
                onCategoryChange = viewModel::onCategoryFilterChange,
                onProjectChange = viewModel::onProjectFilterChange,
                onClear = viewModel::clearFilters
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.groups.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    uiState.groups.forEach { group ->
                        item {
                            MonthHeader(group.monthYear)
                        }
                        items(group.items) { item ->
                            TransactionRow(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterBar(
    state: TimelineUiState,
    onOwnerChange: (String?) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onProjectChange: (Long?) -> Unit,
    onClear: () -> Unit
) {
    var showOwnerMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showProjectMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Owner Filter
        FilterChip(
            selected = state.filterOwner != null,
            onClick = { showOwnerMenu = true },
            label = { Text(state.filterOwner ?: "All Owners") },
            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(18.dp)) }
        )
        DropdownMenu(expanded = showOwnerMenu, onDismissRequest = { showOwnerMenu = false }) {
            DropdownMenuItem(text = { Text("All Owners") }, onClick = { onOwnerChange(null); showOwnerMenu = false })
            state.allOwners.forEach { owner ->
                DropdownMenuItem(text = { Text(owner) }, onClick = { onOwnerChange(owner); showOwnerMenu = false })
            }
        }

        // Category Filter
        FilterChip(
            selected = state.filterCategoryId != null,
            onClick = { showCategoryMenu = true },
            label = { Text(state.allCategories.find { it.id == state.filterCategoryId }?.name ?: "All Categories") },
            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(18.dp)) }
        )
        DropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) {
            DropdownMenuItem(text = { Text("All Categories") }, onClick = { onCategoryChange(null); showCategoryMenu = false })
            state.allCategories.forEach { category ->
                DropdownMenuItem(text = { Text(category.name) }, onClick = { onCategoryChange(category.id); showCategoryMenu = false })
            }
        }

        // Project Filter
        FilterChip(
            selected = state.filterProjectId != null,
            onClick = { showProjectMenu = true },
            label = { Text(state.allProjects.find { it.id == state.filterProjectId }?.name ?: "All Projects") },
            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(18.dp)) }
        )
        DropdownMenu(expanded = showProjectMenu, onDismissRequest = { showProjectMenu = false }) {
            DropdownMenuItem(text = { Text("All Projects") }, onClick = { onProjectChange(null); showProjectMenu = false })
            state.allProjects.forEach { project ->
                DropdownMenuItem(text = { Text(project.name) }, onClick = { onProjectChange(project.id); showProjectMenu = false })
            }
        }

        if (state.filterOwner != null || state.filterCategoryId != null || state.filterProjectId != null) {
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Clear, contentDescription = "Clear Filters")
            }
        }
    }
}

@Composable
fun MonthHeader(monthYear: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = monthYear,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

@Composable
fun TransactionRow(item: TimelineItem) {
    val transaction = item.transaction
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.isSplitChild) {
            Spacer(modifier = Modifier.width(32.dp))
        }

        // Icon
        Surface(
            shape = CircleShape,
            color = if (item.isSplitChild) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(if (item.isSplitChild) 32.dp else 44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.category?.icon?.let { getIconByName(it) } ?: getIconForType(transaction.type),
                    contentDescription = null,
                    modifier = Modifier.size(if (item.isSplitChild) 16.dp else 24.dp),
                    tint = if (item.isSplitChild) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            val title = if (item.isSplitChild) {
                item.category?.name ?: "Split"
            } else {
                item.category?.name ?: transaction.type.name.lowercase().replace('_', ' ').capitalize()
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Full date (e.g., 17/03/26)
                val formattedDate = java.time.Instant.ofEpochMilli(transaction.date)
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"))

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (item.project != null) {
                    Text(" • ", color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = item.project.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (transaction.note.isNotEmpty()) {
                val isRedundant = item.isSystemEntry && transaction.note.equals(transaction.type.name.replace('_', ' '), ignoreCase = true)
                if (!isRedundant) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
            }
        }

        // Amount & Account
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (transaction.type == TransactionType.EXPENSE) "-${transaction.amountCents.format()}" else transaction.amountCents.format(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when (transaction.type) {
                    TransactionType.INCOME -> Color(0xFF4CAF50)
                    TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = if (transaction.type == TransactionType.TRANSFER && item.targetAccount != null) {
                    "${item.account?.name ?: "Unknown"} -> ${item.targetAccount.name}"
                } else {
                    item.account?.name ?: "Unknown"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No transactions found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
    }
}

fun Long.format(): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    return currencyFormat.format(this / 100.0)
}

fun getIconForType(type: TransactionType): ImageVector {
    return when (type) {
        TransactionType.EXPENSE -> Icons.Default.RemoveCircleOutline
        TransactionType.INCOME -> Icons.Default.AddCircleOutline
        TransactionType.TRANSFER -> Icons.Default.SyncAlt
        TransactionType.OPENING_BALANCE -> Icons.Default.AccountBalance
        TransactionType.REVALUATION -> Icons.Default.Balance
        TransactionType.RECONCILIATION_ADJUSTMENT -> Icons.AutoMirrored.Filled.CompareArrows
    }
}

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
