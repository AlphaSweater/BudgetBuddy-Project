//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetReport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.usecase.main.budget.GetBudgetReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetReportViewModel @Inject constructor(
    private val getBudgetReportUseCase: GetBudgetReportUseCase
) : ViewModel() {

    private val _transactions = MutableLiveData<List<BudgetListItems>>()
    val transactions: LiveData<List<BudgetListItems>> = _transactions

    private val _categories = MutableLiveData<List<BudgetListItems>>()
    val categories: LiveData<List<BudgetListItems>> = _categories

    private val _selectedBudget = MutableLiveData<Budget>()
    val selectedBudget: LiveData<Budget> = _selectedBudget

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadBudgetReport(budgetId: Long) {
        viewModelScope.launch {
            getBudgetReportUseCase(budgetId)
                .catch { e ->
                    _error.value = e.message ?: "An error occurred"
                }
                .collect { report ->
                    _selectedBudget.value = report.budget
                    _transactions.value = report.transactions
                    _categories.value = report.categories
                }
        }
    }
}