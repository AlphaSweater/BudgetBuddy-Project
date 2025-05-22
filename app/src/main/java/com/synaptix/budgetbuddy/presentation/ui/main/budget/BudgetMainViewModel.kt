package com.synaptix.budgetbuddy.presentation.ui.main.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.budget.GetBudgetsUseCase
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetMainViewModel @Inject constructor(
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

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
}