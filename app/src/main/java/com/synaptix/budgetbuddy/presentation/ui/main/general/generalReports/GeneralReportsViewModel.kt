package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoriesUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralReportsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    sealed class TransactionState {
        object Loading : TransactionState()
        data class Success(val transactions: List<Transaction>) : TransactionState()
        data class Error(val message: String) : TransactionState()
        object Empty : TransactionState()
    }

    sealed class CategoryState {
        object Loading : CategoryState()
        data class Success(val categories: List<Category>) : CategoryState()
        data class Error(val message: String) : CategoryState()
        object Empty : CategoryState()
    }

    private val _transactionsState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionsState: StateFlow<TransactionState> = _transactionsState

    private val _categoriesState = MutableStateFlow<CategoryState>(CategoryState.Loading)
    val categoriesState: StateFlow<CategoryState> = _categoriesState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _transactionsState.value = TransactionState.Empty
                _categoriesState.value = CategoryState.Empty
                return@launch
            }

            // Load transactions
            launch {
                getTransactionsUseCase.execute(userId)
                    .catch { e ->
                        _transactionsState.value = TransactionState.Error(e.message ?: "Unknown error")
                    }
                    .collect { result ->
                        _transactionsState.value = when (result) {
                            is GetTransactionsUseCase.GetTransactionsResult.Success -> {
                                if (result.transactions.isEmpty()) TransactionState.Empty
                                else TransactionState.Success(result.transactions)
                            }
                            is GetTransactionsUseCase.GetTransactionsResult.Error -> 
                                TransactionState.Error("Failed to load transactions")
                        }
                    }
            }

            // Load categories
            launch {
                getCategoriesUseCase.execute(userId)
                    .catch { e ->
                        _categoriesState.value = CategoryState.Error(e.message ?: "Unknown error")
                    }
                    .collect { result ->
                        _categoriesState.value = when (result) {
                            is GetCategoriesUseCase.GetCategoriesResult.Success -> {
                                if (result.categories.isEmpty()) CategoryState.Empty
                                else CategoryState.Success(result.categories)
                            }
                            is GetCategoriesUseCase.GetCategoriesResult.Error -> 
                                CategoryState.Error("Failed to load categories")
                        }
                    }
            }
        }
    }

    fun getTransactionsByType(type: String): List<Transaction> {
        return when (val state = transactionsState.value) {
            is TransactionState.Success -> state.transactions.filter { it.category.type.equals(type, ignoreCase = true) }
            else -> emptyList()
        }
    }

    fun getCategoriesByType(type: String): List<Category> {
        return when (val state = categoriesState.value) {
            is CategoryState.Success -> state.categories.filter { it.type.equals(type, ignoreCase = true) }
            else -> emptyList()
        }
    }
}