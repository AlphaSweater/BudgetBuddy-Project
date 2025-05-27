package com.synaptix.budgetbuddy.presentation.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoriesUseCase
import com.synaptix.budgetbuddy.core.usecase.main.display.TotalWalletUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getTotalWalletUseCase: TotalWalletUseCase
) : ViewModel() {

    sealed class WalletState {
        object Loading : WalletState()
        data class Success(val wallets: List<Wallet>) : WalletState()
        data class Error(val message: String) : WalletState()
    }

    sealed class TransactionState {
        object Loading : TransactionState()
        data class Success(val transactions: List<Transaction>) : TransactionState()
        data class Error(val message: String) : TransactionState()
    }

    sealed class CategoryState {
        object Loading : CategoryState()
        data class Success(val categories: List<Category>) : CategoryState()
        data class Error(val message: String) : CategoryState()
    }

    private val _walletsState = MutableStateFlow<WalletState>(WalletState.Loading)
    val walletsState: StateFlow<WalletState> = _walletsState

    private val _transactionsState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionsState: StateFlow<TransactionState> = _transactionsState

    private val _categoriesState = MutableStateFlow<CategoryState>(CategoryState.Loading)
    val categoriesState: StateFlow<CategoryState> = _categoriesState

    //for total wallet balance
    private val _totalWalletBalance = MutableStateFlow<Double?>(null)
    val totalWalletBalance: StateFlow<Double?> get() = _totalWalletBalance

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var _selectedStartDate = ""
    var selectedStartDate: String
        get() = _selectedStartDate
        set(value) {
            _selectedStartDate = value
            loadTransactions()
        }

    private var _selectedEndDate = ""
    var selectedEndDate: String
        get() = _selectedEndDate
        set(value) {
            _selectedEndDate = value
            loadTransactions()
        }

    private val _selectedTransaction = MutableLiveData<Transaction>()
    val selectedTransaction: LiveData<Transaction> = _selectedTransaction

    private var currentFilter: TransactionFilter = TransactionFilter.ALL
        set(value) {
            field = value
            loadTransactions()
        }

    init {
        refreshData()
    }

    fun refreshData() {
        loadWallets()
        loadTransactions()
        loadCategories()
        loadTotalWalletBalance()
    }

    private fun loadWallets() {
        viewModelScope.launch {
            _walletsState.value = WalletState.Loading
            try {
                val userId = getUserIdUseCase.execute()
                when (val result = getWalletUseCase.execute(userId)) {
                    is GetWalletUseCase.GetWalletResult.Success -> {
                        _walletsState.value = WalletState.Success(result.wallets)
                    }
                    is GetWalletUseCase.GetWalletResult.Error -> {
                        _walletsState.value = WalletState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _walletsState.value = WalletState.Error(e.message ?: "Failed to load wallets")
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _transactionsState.value = TransactionState.Loading
            try {
                val userId = getUserIdUseCase.execute()
                if (userId.isEmpty()) {
                    _transactionsState.value = TransactionState.Error("User ID is empty")
                    return@launch
                }

                val result = if (_selectedStartDate.isNotEmpty() && _selectedEndDate.isNotEmpty()) {
                    val startDate = dateFormat.parse(_selectedStartDate)?.time ?: 0L
                    val endDate = dateFormat.parse(_selectedEndDate)?.time ?: 0L
                    getTransactionsUseCase.executeWithDateRange(userId, startDate, endDate)
                } else {
                    getTransactionsUseCase.execute(userId)
                }

                when (result) {
                    is GetTransactionsUseCase.GetTransactionsResult.Success -> {
                        val filtered = filterTransactions(result.transactions)
                        _transactionsState.value = TransactionState.Success(filtered)
                    }
                    is GetTransactionsUseCase.GetTransactionsResult.Error -> {
                        _transactionsState.value = TransactionState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _transactionsState.value = TransactionState.Error(e.message ?: "Failed to load transactions")
            }
        }
    }

    private fun filterTransactions(transactions: List<Transaction>): List<Transaction> {
        var filtered = transactions

        // Apply current filter
        filtered = when (currentFilter) {
            TransactionFilter.ALL -> filtered
            TransactionFilter.TODAY -> filtered.filter { isToday(it.date) }
            TransactionFilter.THIS_WEEK -> filtered.filter { isThisWeek(it.date) }
            TransactionFilter.THIS_MONTH -> filtered.filter { isThisMonth(it.date) }
        }

        return filtered.sortedByDescending { it.date }
    }

    private fun isToday(date: Long): Boolean {
        return try {
            val dateDate = dateFormat.format(Date(date))
            val today = Calendar.getInstance()
            val transactionCal = Calendar.getInstance().apply { time = dateFormat.parse(dateDate) ?: Date() }

            today.get(Calendar.YEAR) == transactionCal.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == transactionCal.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            false
        }
    }

    private fun isThisWeek(date: Long): Boolean {
        return try {
            val dateDate = dateFormat.format(Date(date))
            val today = Calendar.getInstance()
            val transactionCal = Calendar.getInstance().apply { time = dateFormat.parse(dateDate) ?: Date() }

            today.get(Calendar.YEAR) == transactionCal.get(Calendar.YEAR) &&
                    today.get(Calendar.WEEK_OF_YEAR) == transactionCal.get(Calendar.WEEK_OF_YEAR)
        } catch (e: Exception) {
            false
        }
    }

    private fun isThisMonth(date: Long): Boolean {
        return try {
            val dateDate = dateFormat.format(Date(date))
            val today = Calendar.getInstance()
            val transactionCal = Calendar.getInstance().apply { time = dateFormat.parse(dateDate) ?: Date() }

            today.get(Calendar.YEAR) == transactionCal.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == transactionCal.get(Calendar.MONTH)
        } catch (e: Exception) {
            false
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = CategoryState.Loading
            try {
                val userId = getUserIdUseCase.execute()
                when (val result = getCategoriesUseCase.execute(userId)) {
                    is GetCategoriesUseCase.GetCategoriesResult.Success -> {
                        _categoriesState.value = CategoryState.Success(result.categories)
                    }
                    is GetCategoriesUseCase.GetCategoriesResult.Error -> {
                        _categoriesState.value = CategoryState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _categoriesState.value = CategoryState.Error(e.message ?: "Failed to load categories")
            }
        }
    }

    fun loadTotalWalletBalance() {
        viewModelScope.launch {
            try {
                val userId = getUserIdUseCase.execute()
                val total = getTotalWalletUseCase.execute(userId)
                _totalWalletBalance.value = total
            } catch (e: Exception) {
                _totalWalletBalance.value = 0.0
            }
        }
    }

    fun setTransaction(transaction: Transaction) {
        _selectedTransaction.value = transaction
    }

    fun setTransactionFilter(filter: TransactionFilter) {
        currentFilter = filter
    }

    fun clearDateFilter() {
        _selectedStartDate = ""
        _selectedEndDate = ""
        loadTransactions()
    }
}

enum class TransactionFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH
}
