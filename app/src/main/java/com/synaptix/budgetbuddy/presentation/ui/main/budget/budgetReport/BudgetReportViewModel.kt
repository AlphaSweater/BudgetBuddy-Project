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

//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.synaptix.budgetbuddy.core.model.Budget
//import com.synaptix.budgetbuddy.core.model.BudgetListItems
//import com.synaptix.budgetbuddy.data.firebase.model.BudgetDTO
//import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class BudgetReportViewModel @Inject constructor(
//    private val firestoreBudgetRepository: FirestoreBudgetRepository
//) : ViewModel() {
//
//    private val _transactions = MutableLiveData<List<BudgetListItems>>()
//    val transactions: LiveData<List<BudgetListItems>> = _transactions
//
//    private val _categories = MutableLiveData<List<BudgetListItems>>()
//    val categories: LiveData<List<BudgetListItems>> = _categories
//
//    private val _selectedBudget = MutableLiveData<Budget>()
//    val selectedBudget: LiveData<Budget> = _selectedBudget
//
//    private val _error = MutableLiveData<String>()
//    val error: LiveData<String> = _error
//
//    fun loadBudgetReport(budgetId: String) {
//        viewModelScope.launch {
//            try {
//                firestoreBudgetRepository.getBudgetById(budgetId)
//                    .collectLatest { result ->
//                        when (result) {
//                            is com.synaptix.budgetbuddy.core.model.Result.Success -> {
//                                result.data?.let { budget ->
//                                    _selectedBudget.value = budget.toDomain()
//                                    // TODO: Load transactions and categories for this budget
//                                }
//                            }
//
//                            is com.synaptix.budgetbuddy.core.model.Result.Error -> {
//                                _error.value = result.exception.message
//                            }
//                        }
//                    }
//            } catch (e: Exception) {
//                _error.value = e.message
//            }
//        }
//    }
//}