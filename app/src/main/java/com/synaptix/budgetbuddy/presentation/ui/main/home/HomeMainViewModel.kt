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
    private val getUserIdUseCase: GetUserIdUseCase
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

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

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

    private var currentFilter: TransactionFilter = TransactionFilter.ALL
        set(value) {
            field = value
            loadTransactions()
        }

    // Simplified caching
    private var cachedTransactions: List<Transaction> = emptyList()
    private var lastTransactionFetchTime: Long = 0
    private val CACHE_DURATION = 30000L // 30 seconds cache

    fun getCachedTransactions(): List<Transaction> = cachedTransactions

    init {
        refreshData()
    }

    fun refreshData(forceRefresh: Boolean = false) {
        if (forceRefresh || isCacheStale()) {
            cachedTransactions = emptyList()
            lastTransactionFetchTime = 0
        }
        loadWallets()
        loadTransactions()
        loadCategories()
    }

    private suspend fun getTransactions(userId: String): List<Transaction> {
        val currentTime = System.currentTimeMillis()
        
        if (!isCacheStale() && cachedTransactions.isNotEmpty()) {
            return cachedTransactions
        }

        return fetchTransactions(userId).also {
            cachedTransactions = it
            lastTransactionFetchTime = currentTime
        }
    }

    private suspend fun fetchTransactions(userId: String): List<Transaction> {
        val result = if (_selectedStartDate.isNotEmpty() && _selectedEndDate.isNotEmpty()) {
            val startDate = dateFormat.parse(_selectedStartDate)?.time ?: 0L
            val endDate = dateFormat.parse(_selectedEndDate)?.time ?: 0L
            getTransactionsUseCase.executeWithDateRange(userId, startDate, endDate)
        } else {
            getTransactionsUseCase.execute(userId)
        }

        return when (result) {
            is GetTransactionsUseCase.GetTransactionsResult.Success -> result.transactions
            else -> emptyList()
        }
    }

    fun isCacheStale(): Boolean = System.currentTimeMillis() - lastTransactionFetchTime > CACHE_DURATION

    private fun loadWallets() {
        viewModelScope.launch {
            _walletsState.value = WalletState.Loading
            try {
                val userId = getUserIdUseCase.execute()
                when (val result = getWalletUseCase.execute(userId)) {
                    is GetWalletUseCase.GetWalletResult.Success -> {
                        val transactions = getTransactions(userId)
                        val sortedWallets = result.wallets.sortedByDescending { wallet ->
                            transactions
                                .filter { it.wallet.id == wallet.id }
                                .maxOfOrNull { it.date } ?: 0L
                        }
                        _walletsState.value = WalletState.Success(sortedWallets)
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

                val transactions = getTransactions(userId)
                val filtered = filterTransactions(transactions)
                _transactionsState.value = TransactionState.Success(filtered)
            } catch (e: Exception) {
                _transactionsState.value = TransactionState.Error(e.message ?: "Failed to load transactions")
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

    private fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = CategoryState.Loading
            try {
                val userId = getUserIdUseCase.execute()
                when (val result = getCategoriesUseCase.execute(userId)) {
                    is GetCategoriesUseCase.GetCategoriesResult.Success -> {
                        val transactions = getTransactions(userId)
                        val categoryAmounts = result.categories.associateWith { category ->
                            transactions
                                .filter { it.category.id == category.id }
                                .sumOf { it.amount }
                        }
                        val sortedCategories = result.categories.sortedByDescending { category ->
                            categoryAmounts[category] ?: 0.0
                        }
                        _categoriesState.value = CategoryState.Success(sortedCategories)
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
