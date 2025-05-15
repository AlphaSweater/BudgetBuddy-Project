package com.synaptix.budgetbuddy.presentation.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetCategoriesUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeMainViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getTransactionUseCase: GetTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success<T>(val data: T) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _walletsState = MutableStateFlow<UiState>(UiState.Loading)
    val walletsState: StateFlow<UiState> = _walletsState

    private val _transactionsState = MutableStateFlow<UiState>(UiState.Loading)
    val transactionsState: StateFlow<UiState> = _transactionsState

    private val _categoriesState = MutableStateFlow<UiState>(UiState.Loading)
    val categoriesState: StateFlow<UiState> = _categoriesState

    private var _selectedStartDate = ""
    var selectedStartDate: String
        get() = _selectedStartDate
        set(value) {
            _selectedStartDate = value
            refreshData()
        }

    private var _selectedEndDate = ""
    var selectedEndDate: String
        get() = _selectedEndDate
        set(value) {
            _selectedEndDate = value
            refreshData()
        }

    private val _selectedTransaction = MutableLiveData<Transaction>()
    val selectedTransaction: LiveData<Transaction> = _selectedTransaction

    init {
        refreshData()
    }

    fun refreshData() {
        loadWallets()
        loadTransactions()
        loadCategories()
    }

    private fun loadWallets() {
        viewModelScope.launch {
            _walletsState.value = UiState.Loading
            try {
                val userId = getUserIdUseCase.execute()
                val walletList = getWalletUseCase.execute(userId)
                _walletsState.value = UiState.Success(walletList)
            } catch (e: Exception) {
                _walletsState.value = UiState.Error(e.message ?: "Failed to load wallets")
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _transactionsState.value = UiState.Loading
            try {
                val userId = getUserIdUseCase.execute()
                val transactionList = getTransactionUseCase.execute(userId)
                    .let { transactions ->
                        if (_selectedStartDate.isNotEmpty() && _selectedEndDate.isNotEmpty()) {
                            transactions.filter { transaction ->
                                // Add your date filtering logic here
                                true // Placeholder
                            }
                        } else {
                            transactions
                        }
                    }
                _transactionsState.value = UiState.Success(transactionList)
            } catch (e: Exception) {
                _transactionsState.value = UiState.Error(e.message ?: "Failed to load transactions")
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading
            try {
                val userId = getUserIdUseCase.execute()
                val categoryList = getCategoriesUseCase.invoke(userId)
                _categoriesState.value = UiState.Success(categoryList)
            } catch (e: Exception) {
                _categoriesState.value = UiState.Error(e.message ?: "Failed to load categories")
            }
        }
    }

    fun setTransaction(transaction: Transaction) {
        _selectedTransaction.value = transaction
    }
}
