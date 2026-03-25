package com.familyfinance.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyfinance.domain.model.*
import com.familyfinance.domain.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

data class TimelineItem(
    val transaction: Transaction,
    val account: Account?,
    val category: Category?,
    val project: Project?,
    val isSplitChild: Boolean = false,
    val isSplitRoot: Boolean = false,
    val isSystemEntry: Boolean = false,
    val targetAccount: Account? = null
)

data class TimelineGroup(
    val monthYear: String,
    val items: List<TimelineItem>
)

data class TimelineUiState(
    val groups: List<TimelineGroup> = emptyList(),
    val isLoading: Boolean = true,
    val filterOwner: String? = null,
    val filterCategoryId: Long? = null,
    val filterProjectId: Long? = null,
    val allCategories: List<Category> = emptyList(),
    val allProjects: List<Project> = emptyList(),
    val allOwners: List<String> = emptyList()
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val filterFlow = _uiState.map { 
            Triple(it.filterOwner, it.filterCategoryId, it.filterProjectId) 
        }.distinctUntilChanged()

        combine(
            repository.getTransactionsFlow(),
            repository.getAccountsFlow(),
            repository.getCategoriesFlow(),
            repository.getProjectsFlow(),
            filterFlow
        ) { transactions: List<Transaction>, 
            accounts: List<Account>, 
            categories: List<Category>, 
            projects: List<Project>, 
            filters: Triple<String?, Long?, Long?> ->
            
            val (owner, categoryId, projectId) = filters
            
            val filteredTransactions = transactions
                .filter { it.type != TransactionType.OPENING_BALANCE || owner == null }
                .filter { trans ->
                    val acc = accounts.find { it.id == trans.accountId }
                    (owner == null || acc?.ownerLabel == owner) &&
                    (categoryId == null || trans.categoryId == categoryId) &&
                    (projectId == null || trans.projectId == projectId)
                }
                .sortedByDescending { it.date }

            val items = filteredTransactions.map { trans ->
                val acc = accounts.find { it.id == trans.accountId }
                val cat = categories.find { it.id == trans.categoryId }
                val proj = projects.find { it.id == trans.projectId }
                
                // Identify split root/child
                val isSplit = trans.receiptGroupId != null
                var isRoot = false
                var isChild = false
                if (isSplit) {
                    val firstInGroup = filteredTransactions.findLast { it.receiptGroupId == trans.receiptGroupId }
                    if (firstInGroup?.id == trans.id) isRoot = true else isChild = true
                }

                TimelineItem(
                    transaction = trans,
                    account = acc,
                    category = cat,
                    project = proj,
                    isSplitChild = isChild,
                    isSplitRoot = isRoot,
                    isSystemEntry = trans.type in listOf(
                        TransactionType.OPENING_BALANCE,
                        TransactionType.REVALUATION,
                        TransactionType.RECONCILIATION_ADJUSTMENT
                    ),
                    targetAccount = accounts.find { it.id == trans.targetAccountId }
                )
            }

            val groups = items.groupBy { item ->
                val date = Instant.ofEpochMilli(item.transaction.date)
                    .atZone(ZoneId.systemDefault())
                val month = date.month.getDisplayName(TextStyle.FULL, Locale.US)
                val year = date.year
                "$month $year"
            }.map { (monthYear, items) ->
                TimelineGroup(monthYear, items)
            }

            TimelineUiState(
                groups = groups,
                isLoading = false,
                filterOwner = owner,
                filterCategoryId = categoryId,
                filterProjectId = projectId,
                allCategories = categories,
                allProjects = projects,
                allOwners = accounts.mapNotNull { it.ownerLabel }.distinct()
            )
        }.onEach { state: TimelineUiState ->
            _uiState.update { state }
        }.launchIn(viewModelScope)
    }

    fun onOwnerFilterChange(owner: String?) {
        _uiState.update { it.copy(filterOwner = owner) }
    }

    fun onCategoryFilterChange(categoryId: Long?) {
        _uiState.update { it.copy(filterCategoryId = categoryId) }
    }

    fun onProjectFilterChange(projectId: Long?) {
        _uiState.update { it.copy(filterProjectId = projectId) }
    }

    fun clearFilters() {
        _uiState.update { it.copy(filterOwner = null, filterCategoryId = null, filterProjectId = null) }
    }
}
