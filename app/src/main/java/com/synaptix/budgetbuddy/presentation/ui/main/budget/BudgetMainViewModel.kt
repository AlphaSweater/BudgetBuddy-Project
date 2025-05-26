package com.synaptix.budgetbuddy.presentation.ui.main.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.budget.GetBudgetsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.display.BudgetSummary
import com.synaptix.budgetbuddy.core.usecase.main.display.TotalBudgetUseCase
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetMainViewModel @Inject constructor(
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val totalBudgetUseCase: TotalBudgetUseCase
) : ViewModel() {

    private val _budgetSummary = MutableLiveData<BudgetSummary>()
    val budgetSummary: LiveData<BudgetSummary> = _budgetSummary

    private val _budgets = MutableLiveData<List<Budget>>()
    val budgets: LiveData<List<Budget>> = _budgets

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchBudgets() {
        viewModelScope.launch {
            try {
                val userId = getUserIdUseCase.execute()
                if (userId.isEmpty()) {
                    _error.value = "User ID is empty"
                    return@launch
                }

                when (val result = getBudgetsUseCase.execute(userId)) {
                    is GetBudgetsUseCase.GetBudgetsResult.Success -> {
                        _budgets.value = result.budgets
                        _error.value = null
                    }
                    is GetBudgetsUseCase.GetBudgetsResult.Error -> {
                        _error.value = result.message
                        _budgets.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
                _budgets.value = emptyList()
            }
        }
    }


    fun fetchBudgetSummary() {
        viewModelScope.launch {
            try {
                val userId = getUserIdUseCase.execute()
                if (userId.isEmpty()) {
                    _error.value = "User ID is empty"
                    return@launch
                }
                val summary = totalBudgetUseCase.execute(userId)
                _budgetSummary.value = summary
            } catch (e: Exception) {
                _error.value = e.message ?: "Error getting budget summary"
            }
        }
    }
}