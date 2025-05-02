package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd

import androidx.lifecycle.ViewModel
import com.synaptix.budgetbuddy.core.model.BudgetIn
import com.synaptix.budgetbuddy.core.usecase.main.budget.AddBudgetUseCase
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetAddViewModel @Inject constructor(
    private val addBudgetUseCase: AddBudgetUseCase
) : ViewModel() {

    fun addBudget(budgetIn: BudgetIn) {
        viewModelScope.launch {
            addBudgetUseCase(budgetIn)
        }
    }
}