package com.synaptix.budgetbuddy.presentation.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoriesUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeMainViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    sealed class WalletState {
        object Loading : WalletState()
        data class Success(val wallets: List<Wallet>) : WalletState()
        data class Error(val message: String) : WalletState()
        object Empty : WalletState()
    }

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

    private val _walletsState = MutableStateFlow<WalletState>(WalletState.Loading)
    val walletsState: StateFlow<WalletState> = _walletsState

    private val _transactionsState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionsState: StateFlow<TransactionState> = _transactionsState

    private val _categoriesState = MutableStateFlow<CategoryState>(CategoryState.Loading)
    val categoriesState: StateFlow<CategoryState> = _categoriesState

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

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

    private var currentFilter: TransactionFilter = TransactionFilter.ALL
        set(value) {
            field = value
            refreshData()
        }

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _walletsState.value = WalletState.Empty
                _transactionsState.value = TransactionState.Empty
                _categoriesState.value = CategoryState.Empty
                return@launch
            }

            launch {
                // Collect transactions
                val transactionsFlow = if (_selectedStartDate.isNotEmpty() && _selectedEndDate.isNotEmpty()) {
                    val startDate = dateFormat.parse(_selectedStartDate)?.time ?: 0L
                    val endDate = dateFormat.parse(_selectedEndDate)?.time ?: 0L
                    getTransactionsUseCase.executeWithDateRange(userId, startDate, endDate)
                } else {
                    getTransactionsUseCase.execute(userId)
                }

                transactionsFlow
                    .catch { e ->
                        _transactionsState.value = TransactionState.Empty
                    }
                    .collect { result ->
                        when (result) {
                            is GetTransactionsUseCase.GetTransactionsResult.Success -> {
                                val filtered = filterTransactions(result.transactions)
                                _transactionsState.value = if (filtered.isEmpty()) TransactionState.Empty
                                else TransactionState.Success(filtered)
                            }
                            is GetTransactionsUseCase.GetTransactionsResult.Error -> {
                                _transactionsState.value = TransactionState.Empty
                            }
                        }
                    }
            }

            // Launch parallel coroutines for each collection
            launch {
                // Collect wallets
                getWalletUseCase.execute(userId)
                    .catch { e -> 
                        _walletsState.value = WalletState.Empty
                    }
                    .collect { result ->
                        _walletsState.value = when (result) {
                            is GetWalletUseCase.GetWalletResult.Success -> {
                                if (result.wallets.isEmpty()) WalletState.Empty
                                else WalletState.Success(result.wallets)
                            }
                            is GetWalletUseCase.GetWalletResult.Error -> WalletState.Empty
                        }
                    }
            }

            launch {
                // Collect categories
                getCategoriesUseCase.execute(userId)
                    .catch { e ->
                        _categoriesState.value = CategoryState.Empty
                    }
                    .collect { result ->
                        _categoriesState.value = when (result) {
                            is GetCategoriesUseCase.GetCategoriesResult.Success -> {
                                if (result.categories.isEmpty()) CategoryState.Empty
                                else CategoryState.Success(result.categories)
                            }
                            is GetCategoriesUseCase.GetCategoriesResult.Error -> CategoryState.Empty
                        }
                    }
            }
        }
    }

    private fun filterTransactions(transactions: List<Transaction>): List<Transaction> {
        val filtered = when (currentFilter) {
            TransactionFilter.ALL -> transactions
            TransactionFilter.TODAY -> transactions.filter { isDateInRange(it.date, Calendar.DAY_OF_YEAR) }
            TransactionFilter.THIS_WEEK -> transactions.filter { isDateInRange(it.date, Calendar.WEEK_OF_YEAR) }
            TransactionFilter.THIS_MONTH -> transactions.filter { isDateInRange(it.date, Calendar.MONTH) }
        }
        return filtered.sortedByDescending { it.date }
    }

    private fun isDateInRange(date: Long, calendarField: Int): Boolean {
        val transactionDate = Calendar.getInstance().apply { 
            time = Date(date)
        }
        val today = Calendar.getInstance()
        
        return today.get(Calendar.YEAR) == transactionDate.get(Calendar.YEAR) &&
               today.get(calendarField) == transactionDate.get(calendarField)
    }

    fun setTransactionFilter(filter: TransactionFilter) {
        currentFilter = filter
    }

    fun clearDateFilter() {
        _selectedStartDate = ""
        _selectedEndDate = ""
        refreshData()
    }
}

enum class TransactionFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH
}
