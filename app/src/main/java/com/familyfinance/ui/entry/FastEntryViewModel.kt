package com.familyfinance.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyfinance.domain.model.*
import com.familyfinance.domain.repository.FinanceRepository
import com.familyfinance.domain.usecase.SaveSplitReceiptUseCase
import com.familyfinance.domain.usecase.SaveTransactionUseCase
import com.familyfinance.domain.usecase.SaveTransferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplitLine(
    val category: Category? = null,
    val amountCents: Long = 0
)

data class FastEntryUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val totalAmountCents: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val account: Account? = null,
    val targetAccount: Account? = null, // For transfers
    val category: Category? = null,
    val project: Project? = null,
    val note: String = "",
    val splitLines: List<SplitLine> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaved: Boolean = false
) {
    val remainderCents: Long
        get() = totalAmountCents - splitLines.sumOf { it.amountCents }

    val isValid: Boolean
        get() = when (type) {
            TransactionType.EXPENSE, TransactionType.INCOME -> {
                totalAmountCents > 0 && account != null && (category != null || splitLines.isNotEmpty())
            }
            TransactionType.TRANSFER -> {
                totalAmountCents > 0 && account != null && targetAccount != null && account.id != targetAccount.id
            }
            else -> false
        }
}

@HiltViewModel
class FastEntryViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val saveSplitReceiptUseCase: SaveSplitReceiptUseCase,
    private val saveTransferUseCase: SaveTransferUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FastEntryUiState())
    val uiState: StateFlow<FastEntryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAccountsFlow(),
                repository.getCategoriesFlow(),
                repository.getProjectsFlow()
            ) { accounts, categories, projects ->
                _uiState.update { it.copy(
                    accounts = accounts,
                    categories = categories,
                    projects = projects,
                    isLoading = false
                ) }
            }.collect()
        }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(type = type, category = null, splitLines = emptyList()) }
    }

    fun onAmountChange(amountCents: Long) {
        _uiState.update { it.copy(totalAmountCents = amountCents) }
    }

    fun onAccountChange(account: Account) {
        _uiState.update { it.copy(account = account) }
    }

    fun onTargetAccountChange(account: Account) {
        _uiState.update { it.copy(targetAccount = account) }
    }

    fun onCategoryChange(category: Category) {
        _uiState.update { it.copy(category = category) }
    }

    fun onProjectChange(project: Project?) {
        _uiState.update { it.copy(project = project) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun addSplit() {
        _uiState.update { it.copy(splitLines = it.splitLines + SplitLine()) }
    }

    fun updateSplit(index: Int, split: SplitLine) {
        _uiState.update { state ->
            val updatedSplits = state.splitLines.toMutableList()
            updatedSplits[index] = split
            state.copy(splitLines = updatedSplits)
        }
    }

    fun removeSplit(index: Int) {
        _uiState.update { state ->
            val updatedSplits = state.splitLines.toMutableList()
            updatedSplits.removeAt(index)
            state.copy(splitLines = updatedSplits)
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            val result = when {
                state.type == TransactionType.TRANSFER -> {
                    saveTransferUseCase(
                        date = state.date,
                        amountCents = state.totalAmountCents,
                        fromAccountId = state.account!!.id,
                        toAccountId = state.targetAccount!!.id,
                        note = state.note
                    )
                }
                state.splitLines.isNotEmpty() -> {
                    val transactions = state.splitLines.map { split ->
                        Transaction(
                            date = state.date,
                            amountCents = split.amountCents,
                            accountId = state.account!!.id,
                            categoryId = split.category?.id,
                            projectId = state.project?.id,
                            note = state.note,
                            type = state.type,
                            currencyCode = state.account!!.currency
                        )
                    }
                    saveSplitReceiptUseCase(state.totalAmountCents, transactions)
                }
                else -> {
                    saveTransactionUseCase(
                        Transaction(
                            date = state.date,
                            amountCents = state.totalAmountCents,
                            accountId = state.account!!.id,
                            categoryId = state.category?.id,
                            projectId = state.project?.id,
                            note = state.note,
                            type = state.type,
                            currencyCode = state.account!!.currency
                        )
                    )
                }
            }

            if (result.isSuccess) {
                _uiState.update { it.copy(isSaved = true) }
            } else {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Unknown error") }
            }
        }
    }
}
