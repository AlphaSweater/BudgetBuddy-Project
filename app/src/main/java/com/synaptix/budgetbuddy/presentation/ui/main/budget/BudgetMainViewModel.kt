package com.synaptix.budgetbuddy.presentation.ui.main.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.budget.GetBudgetsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.display.BudgetSummary
import com.synaptix.budgetbuddy.core.usecase.main.display.TotalBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetMainViewModel @Inject constructor(
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val totalBudgetUseCase: TotalBudgetUseCase
) : ViewModel() {

    // Define state classes for UI
    sealed class BudgetState {
        object Loading : BudgetState()
        data class Success(val budgets: List<Budget>) : BudgetState()
        data class Error(val message: String) : BudgetState()
        object Empty : BudgetState()
    }

    private val _budgetState = MutableStateFlow<BudgetState>(BudgetState.Loading)
    val budgetState: StateFlow<BudgetState> = _budgetState

    private val _budgetSummary = MutableStateFlow<BudgetSummary?>(null)
    val budgetSummary: StateFlow<BudgetSummary?> = _budgetSummary

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchBudgets()
        fetchBudgetSummary()
    }

    fun fetchBudgets() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _budgetState.value = BudgetState.Error("User ID is empty")
                return@launch
            }

            getBudgetsUseCase.execute(userId)
                .catch { e ->
                    _budgetState.value = BudgetState.Error(e.message ?: "Unknown error")
                }
                .collect { result ->
                    when (result) {
                        is GetBudgetsUseCase.GetBudgetsResult.Success -> {
                            if (result.budgets.isEmpty()) {
                                _budgetState.value = BudgetState.Empty
                            } else {
                                _budgetState.value = BudgetState.Success(result.budgets)
                            }
                        }
                        is GetBudgetsUseCase.GetBudgetsResult.Error -> {
                            _budgetState.value = BudgetState.Error(result.message)
                        }
                    }
                }
        }
    }

    fun fetchBudgetSummary() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _error.value = "User ID is empty"
                return@launch
            }

            try {
                val summary = totalBudgetUseCase.execute(userId)
                _budgetSummary.value = summary
            } catch (e: Exception) {
                _error.value = e.message ?: "Error getting budget summary"
            }
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\