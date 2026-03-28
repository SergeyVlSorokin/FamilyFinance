package com.familyfinance.ui.entry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.familyfinance.domain.model.Account
import com.familyfinance.domain.model.Category
import com.familyfinance.domain.model.Project
import com.familyfinance.domain.model.TransactionType
import com.familyfinance.ui.components.CurrencyInput
import com.familyfinance.ui.components.getIconByName
import com.familyfinance.ui.components.toCents
import com.familyfinance.ui.components.toDecimalString
import java.text.SimpleDateFormat
import java.util.*

// @trace TASK-119
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastEntrySheet(
    viewModel: FastEntryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.date)
    val calendar = remember { Calendar.getInstance() }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )
    
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = uiState.isValid && !uiState.isLoading
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                TypeSelector(
                    selectedType = uiState.type,
                    onTypeSelected = viewModel::onTypeChange
                )

                Spacer(modifier = Modifier.height(32.dp))

                AmountField(
                    amountCents = uiState.totalAmountCents,
                    onAmountChange = viewModel::onAmountChange,
                    currency = uiState.currencyCode
                )

                if (uiState.isFxTransfer) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AmountField(
                        label = "Target Amount (${uiState.targetAccountCurrency})",
                        amountCents = uiState.targetAmountCents,
                        onAmountChange = viewModel::onTargetAmountChange,
                        currency = uiState.targetAccountCurrency
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = dateFormatter.format(Date(uiState.date)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date & Time") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date & Time")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )

                Spacer(modifier = Modifier.height(32.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        AccountPicker(
                            label = if (uiState.type == TransactionType.TRANSFER) "From Account" else "Account",
                            selectedAccount = uiState.account,
                            accounts = uiState.accounts,
                            onAccountSelected = viewModel::onAccountChange
                        )
                    }

                    if (uiState.type == TransactionType.TRANSFER) {
                        item {
                            AccountPicker(
                                label = "To Account",
                                selectedAccount = uiState.targetAccount,
                                accounts = uiState.accounts,
                                onAccountSelected = viewModel::onTargetAccountChange
                            )
                        }
                    } else if (uiState.splitLines.isEmpty()) {
                        item {
                            CategoryPicker(
                                selectedCategory = uiState.category,
                                categories = uiState.categories,
                                onCategorySelected = viewModel::onCategoryChange
                            )
                        }
                    }

                    if (uiState.type != TransactionType.TRANSFER) {
                        item {
                            ProjectPicker(
                                selectedProject = uiState.project,
                                projects = uiState.projects,
                                onProjectSelected = viewModel::onProjectChange
                            )
                        }

                        if (uiState.type == TransactionType.EXPENSE) {
                            item {
                                 SplitSection(
                                     splits = uiState.splitLines,
                                     categories = uiState.categories,
                                     remainderCents = uiState.remainderCents,
                                     currency = uiState.currencyCode,
                                     onAddSplit = viewModel::addSplit,
                                     onUpdateSplit = viewModel::updateSplit,
                                     onRemoveSplit = viewModel::removeSplit
                                 )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = viewModel::onNoteChange,
                            label = { Text("Note (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        )
                    }
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        showTimePicker = true
                    }) {
                        Text("Next")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val dateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = dateMillis
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                        }
                        viewModel.onDateChange(cal.timeInMillis)
                        showTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Select Time") },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(state = timePickerState)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        val types = listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER)
        types.forEachIndexed { index, type ->
            SegmentedButton(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                label = { Text(type.name.lowercase().capitalize(Locale.ROOT)) }
            )
        }
    }
}

@Composable
fun AmountField(
    amountCents: Long,
    onAmountChange: (Long) -> Unit,
    label: String = "Amount",
    currency: String? = null
) {
    CurrencyInput(
        value = amountCents.toDecimalString(),
        onValueChange = { onAmountChange(it.toCents()) },
        label = label,
        currency = currency
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPicker(
    label: String,
    selectedAccount: Account?,
    accounts: List<Account>,
    onAccountSelected: (Account) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedAccount?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = {
                        onAccountSelected(account)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPicker(
    selectedCategory: Category?,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    leadingIcon = {
                        Icon(
                            imageVector = getIconByName(category.icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectPicker(
    selectedProject: Project?,
    projects: List<Project>,
    onProjectSelected: (Project?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedProject?.name ?: "None",
            onValueChange = {},
            readOnly = true,
            label = { Text("Project") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onProjectSelected(null)
                    expanded = false
                }
            )
            projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                        onProjectSelected(project)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SplitSection(
    splits: List<SplitLine>,
    categories: List<Category>,
    remainderCents: Long,
    currency: String?,
    onAddSplit: () -> Unit,
    onUpdateSplit: (Int, SplitLine) -> Unit,
    onRemoveSplit: (Int) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Splits", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onAddSplit) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Split")
            }
        }

        if (splits.isNotEmpty()) {
            splits.forEachIndexed { index, split ->
                SplitItem(
                    split = split,
                    categories = categories,
                    onUpdate = { onUpdateSplit(index, it) },
                    onRemove = { onRemoveSplit(index) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Surface(
                color = if (remainderCents == 0L) MaterialTheme.colorScheme.primaryContainer 
                        else if (remainderCents < 0) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Remainder", fontWeight = FontWeight.Bold)
                    Text("${currency ?: "$"} ${String.format("%.2f", remainderCents / 100.0)}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitItem(
    split: SplitLine,
    categories: List<Category>,
    onUpdate: (SplitLine) -> Unit,
    onRemove: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        var expanded by remember { mutableStateOf(false) }
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1.5f)
        ) {
            OutlinedTextField(
                value = split.category?.name ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
                shape = MaterialTheme.shapes.medium
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = getIconByName(category.icon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            onUpdate(split.copy(category = category))
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        var amountText by remember(split.amountCents) { mutableStateOf(if (split.amountCents == 0L) "" else (split.amountCents / 100.0).toString()) }
        OutlinedTextField(
            value = amountText,
            onValueChange = { 
                if (it.isEmpty() || it.toDoubleOrNull() != null) {
                    amountText = it
                    onUpdate(split.copy(amountCents = (it.toDoubleOrNull() ?: 0.0).let { v -> (v * 100).toLong() }))
                }
            },
            placeholder = { Text("Amount") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = MaterialTheme.shapes.medium
        )

        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove")
        }
    }
}

private fun String.capitalize(locale: Locale): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
