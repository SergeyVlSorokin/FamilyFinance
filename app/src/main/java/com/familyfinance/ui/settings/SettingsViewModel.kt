package com.familyfinance.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyfinance.domain.model.Account
import com.familyfinance.domain.model.Category
import com.familyfinance.domain.model.Project
import com.familyfinance.domain.repository.FinanceRepository
import com.familyfinance.domain.usecase.CreateAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class SettingsEvent {
    object SaveSuccess : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val createAccountUseCase: CreateAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    
    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.getAccountsFlow(),
        repository.getCategoriesFlow(),
        repository.getProjectsFlow(),
        _uiState
    ) { accounts, categories, projects, internalState ->
        internalState.copy(
            accounts = accounts,
            categories = categories,
            projects = projects,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(isLoading = true)
    )

    fun saveAccount(account: Account, openingBalanceCents: Long, openingBalanceTimestamp: Long) {
        viewModelScope.launch {
            if (repository.isAccountKeyTaken(account.name, account.currency, account.ownerLabel)) {
                _uiState.update { it.copy(error = "Account with this name, currency, and owner already exists") }
                return@launch
            }
            createAccountUseCase(account, openingBalanceCents, openingBalanceTimestamp)
            _uiState.update { it.copy(error = null) }
            _events.emit(SettingsEvent.SaveSuccess)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            repository.deleteAccount(id)
        }
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            if (repository.isCategoryNameTaken(category.name)) {
                _uiState.update { it.copy(error = "Category name already taken") }
                return@launch
            }
            repository.saveCategory(category)
            _uiState.update { it.copy(error = null) }
            _events.emit(SettingsEvent.SaveSuccess)
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    fun saveProject(project: Project) {
        viewModelScope.launch {
            if (repository.isProjectNameTaken(project.name)) {
                _uiState.update { it.copy(error = "Project name already taken") }
                return@launch
            }
            repository.saveProject(project)
            _uiState.update { it.copy(error = null) }
            _events.emit(SettingsEvent.SaveSuccess)
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }
}
