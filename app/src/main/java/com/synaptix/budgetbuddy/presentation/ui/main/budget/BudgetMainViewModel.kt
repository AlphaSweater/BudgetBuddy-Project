package com.synaptix.budgetbuddy.presentation.ui.main.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.budget.BudgetCalculationUseCase
import com.synaptix.budgetbuddy.core.usecase.main.budget.GetBudgetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetMainViewModel @Inject constructor(
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val budgetCalculationUseCase: BudgetCalculationUseCase
) : ViewModel() {

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // UI State Classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    sealed class BudgetUiState {
        object Loading : BudgetUiState()
        data class Success(val budgetItems: List<BudgetListItems.BudgetBudgetItem>) : BudgetUiState()
        data class Error(val message: String) : BudgetUiState()
        object Empty : BudgetUiState()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // State Flows
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _budgetUiState = MutableStateFlow<BudgetUiState>(BudgetUiState.Loading)
    val budgetUiState: StateFlow<BudgetUiState> = _budgetUiState

    private val _budgetSummary = MutableStateFlow<BudgetListItems.TotalBudgetsSummary?>(null)
    val budgetSummary: StateFlow<BudgetListItems.TotalBudgetsSummary?> = _budgetSummary

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Data Storage
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    private val _budgetItems = MutableStateFlow<List<BudgetListItems.BudgetBudgetItem>>(emptyList())

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Initialization
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    init {
        fetchBudgets()
        observeBudgetSummary()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Public Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun fetchBudgets() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _budgetUiState.value = BudgetUiState.Error("User ID is empty")
                return@launch
            }

            getBudgetsUseCase.execute(userId)
                .catch { e ->
                    _budgetUiState.value = BudgetUiState.Error(e.message ?: "Unknown error")
                }
                .collect { result ->
                    when (result) {
                        is GetBudgetsUseCase.GetBudgetsResult.Success -> {
                            if (result.budgets.isEmpty()) {
                                _budgetUiState.value = BudgetUiState.Empty
                            } else {
                                _budgets.value = result.budgets
                                initializeBudgetItems(result.budgets)
                            }
                        }
                        is GetBudgetsUseCase.GetBudgetsResult.Error -> {
                            _budgetUiState.value = BudgetUiState.Error(result.message)
                        }
                    }
                }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Private Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun initializeBudgetItems(budgets: List<Budget>) {
        viewModelScope.launch {
            val initialItems = budgets.map { budget ->
                BudgetListItems.BudgetBudgetItem(
                    budget = budget,
                    budgetedAmount = budget.amount,
                    spentAmount = 0.0,
                    remainingAmount = budget.amount
                )
            }
            _budgetItems.value = initialItems
            _budgetUiState.value = BudgetUiState.Success(initialItems)

            // Start observing spent amounts for each budget
            budgets.forEach { budget ->
                observeBudgetSpent(budget)
            }
        }
    }

    private fun observeBudgetSpent(budget: Budget) {
        viewModelScope.launch {
            budgetCalculationUseCase.observeBudgetSpent(budget)
                .catch { e ->
                    _error.value = e.message ?: "Error observing budget spent"
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            updateBudgetItem(budget.id, result.data)
                        }
                        is Result.Error -> {
                            _error.value = result.exception.message
                        }
                    }
                }
        }
    }

    private fun updateBudgetItem(budgetId: String, spentData: BudgetListItems.BudgetBudgetItem) {
        val currentItems = _budgetItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.budget.id == budgetId }
        
        if (index != -1) {
            currentItems[index] = currentItems[index].copy(
                budgetedAmount = spentData.budgetedAmount,
                spentAmount = spentData.spentAmount,
                remainingAmount = spentData.remainingAmount
            )
            _budgetItems.value = currentItems
            _budgetUiState.value = BudgetUiState.Success(currentItems)
        }
    }

    private fun observeBudgetSummary() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _error.value = "User ID is empty"
                return@launch
            }

            budgetCalculationUseCase.observeTotalBudgetsSummary(userId)
                .catch { e ->
                    _error.value = e.message ?: "Error observing budget summary"
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _budgetSummary.value = result.data
                        }
                        is Result.Error -> {
                            _error.value = result.exception.message
                        }
                    }
                }
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\