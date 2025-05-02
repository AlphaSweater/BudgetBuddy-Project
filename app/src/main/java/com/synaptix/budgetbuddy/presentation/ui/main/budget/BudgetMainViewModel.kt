package com.synaptix.budgetbuddy.presentation.ui.main.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.budget.GetBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetMainViewModel @Inject constructor(
    private val getBudgetsUseCase: GetBudgetUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    private val _budgets = MutableLiveData<List<Budget>>()
    val budgets: LiveData<List<Budget>> = _budgets

    fun loadBudgets() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            _budgets.value = getBudgetsUseCase.execute(userId)
        }
    }

}