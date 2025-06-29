package com.synaptix.budgetbuddy.presentation.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.HomeItems
import com.synaptix.budgetbuddy.core.model.HomeListItems
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.TransactionListItems
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoriesUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.TransactionCalculationsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 * 
 * This ViewModel is responsible for:
 * 1. Managing the UI state for wallets, transactions, and categories
 * 2. Loading and filtering data from the backend
 * 3. Handling date range filtering
 * 4. Calculating totals and statistics
 * 5. Managing pie chart data
 * 
 * The ViewModel uses:
 * - StateFlow for UI state management
 * - Coroutines for asynchronous operations
 * - UseCases for business logic
 * 
 * Data Flow:
 * Repository -> UseCase -> ViewModel -> UI
 * 
 * Key Concepts:
 * 1. StateFlow:
 *    - A hot flow that maintains the current state
 *    - Emits updates to all collectors when the state changes
 *    - Unlike LiveData, it's designed for coroutines
 *    - Example: _walletsState emits updates when wallet data changes
 * 
 * 2. Coroutines:
 *    - Lightweight threads for asynchronous operations
 *    - viewModelScope: Coroutine scope tied to ViewModel lifecycle
 *    - launch: Starts a new coroutine
 *    - collect: Collects values from a Flow
 *    - catch: Handles errors in a Flow
 * 
 * 3. Flow:
 *    - Cold stream of values
 *    - Can emit multiple values over time
 *    - Must be collected to start emitting
 *    - Example: getTransactionsUseCase.execute() returns a Flow
 * 
 * 4. State Management:
 *    - Sealed classes represent different states (Loading, Success, Error, Empty)
 *    - StateFlow holds the current state
 *    - UI observes state changes and updates accordingly
 */
@HiltViewModel
class HomeMainViewModel @Inject constructor(
    private val getWalletsUseCase: GetWalletsUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val transactionCalculationsUseCase: TransactionCalculationsUseCase
) : ViewModel() {

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // State Definitions
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    sealed class WalletState {
        object Loading : WalletState()
        data class Success(val wallets: List<Wallet>) : WalletState()
        data class Error(val message: String) : WalletState()
        object Empty : WalletState()
    }

    sealed class TransactionState {
        object Loading : TransactionState()
        data class Success(
            val transactions: List<Transaction>,
            val summary: HomeItems.TransactionsSummary
        ) : TransactionState()
        data class Error(val message: String) : TransactionState()
        object Empty : TransactionState()
    }

    sealed class CategoryState {
        object Loading : CategoryState()
        data class Success(
            val categories: List<Category>,
            val categorySummaries: Map<String, HomeItems.CategoryTransactionsSummary>
        ) : CategoryState()
        data class Error(val message: String) : CategoryState()
        object Empty : CategoryState()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // UI State
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _walletsState = MutableStateFlow<WalletState>(WalletState.Loading)
    val walletsState: StateFlow<WalletState> = _walletsState

    private val _transactionsState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionsState: StateFlow<TransactionState> = _transactionsState

    private val _categoriesState = MutableStateFlow<CategoryState>(CategoryState.Loading)
    val categoriesState: StateFlow<CategoryState> = _categoriesState

    private val _totalWalletBalance = MutableStateFlow<Double?>(null)
    val totalWalletBalance: StateFlow<Double?> = _totalWalletBalance

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Filter State
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Initialization
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    init {
        refreshData()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Data Management
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun refreshData() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                setEmptyStates()
                return@launch
            }

            launch { loadWallets(userId) }
            launch { loadTransactions(userId) }
            launch { loadCategories(userId) }
        }
    }

    private suspend fun loadWallets(userId: String) {
        getWalletsUseCase.execute(userId)
            .catch { e ->
                _walletsState.value = WalletState.Empty
            }
            .collect { result ->
                _walletsState.value = when (result) {
                    is GetWalletsUseCase.GetWalletResult.Success -> {
                        if (result.wallets.isEmpty()) {
                            _totalWalletBalance.value = 0.0
                            WalletState.Empty
                        } else {
                            // Calculate total balance directly from wallets
                            val totalBalance = result.wallets
                                .filter { !it.excludeFromTotal }
                                .sumOf { it.balance }
                            _totalWalletBalance.value = totalBalance
                            // filter the list of wallets in descending order by lastTransactionAt
                            WalletState.Success(result.wallets.sortedByDescending { it.lastTransactionAt })
                        }
                    }
                    is GetWalletsUseCase.GetWalletResult.Error -> {
                        _totalWalletBalance.value = 0.0
                        WalletState.Empty
                    }
                }
            }
    }

    private suspend fun loadTransactions(userId: String) {
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
                        if (filtered.isEmpty()) {
                            _transactionsState.value = TransactionState.Empty
                        } else {
                            val summary = transactionCalculationsUseCase.calculateTotalTransactionsSummary(userId)
                            when (summary) {
                                is Result.Success -> {
                                    _transactionsState.value = TransactionState.Success(
                                        transactions = filtered,
                                        summary = summary.data
                                    )
                                }
                                is Result.Error -> {
                                    _transactionsState.value = TransactionState.Error(summary.exception.message ?: "Unknown error")
                                }
                            }
                        }
                    }
                    is GetTransactionsUseCase.GetTransactionsResult.Error -> {
                        _transactionsState.value = TransactionState.Empty
                    }
                }
            }
    }

    private suspend fun loadCategories(userId: String) {
        getCategoriesUseCase.execute(userId)
            .catch { e ->
                _categoriesState.value = CategoryState.Empty
            }
            .collect { result ->
                _categoriesState.value = when (result) {
                    is GetCategoriesUseCase.GetCategoriesResult.Success -> {
                        if (result.categories.isEmpty()) CategoryState.Empty
                        else {
                            // Filter out income categories
                            val expenseCategories = result.categories.filter { 
                                !it.type.equals("INCOME", ignoreCase = true) 
                            }
                            
                            if (expenseCategories.isEmpty()) {
                                CategoryState.Empty
                            } else {
                                getTransactionsUseCase.execute(userId)
                                    .catch { e ->
                                        _categoriesState.value = CategoryState.Empty
                                    }
                                    .collect { transactionsResult ->
                                        when (transactionsResult) {
                                            is GetTransactionsUseCase.GetTransactionsResult.Success -> {
                                                val categorySummaries = transactionCalculationsUseCase.calculateCategoryTransactionsSummary(
                                                    userId = userId,
                                                    categories = expenseCategories
                                                )
                                                when (categorySummaries) {
                                                    is Result.Success -> {
                                                        // Filter out categories with no transactions and sort by expense
                                                        val categoriesWithTransactions = expenseCategories
                                                            .filter { category ->
                                                                categorySummaries.data[category.id]?.transactionCount ?: 0 > 0
                                                            }
                                                            .sortedByDescending { category ->
                                                                categorySummaries.data[category.id]?.totalExpense ?: 0.0
                                                            }
                                                        
                                                        if (categoriesWithTransactions.isEmpty()) {
                                                            _categoriesState.value = CategoryState.Empty
                                                        } else {
                                                            _categoriesState.value = CategoryState.Success(
                                                                categories = categoriesWithTransactions,
                                                                categorySummaries = categorySummaries.data
                                                            )
                                                        }
                                                    }
                                                    is Result.Error -> {
                                                        _categoriesState.value = CategoryState.Error(categorySummaries.exception.message ?: "Unknown error")
                                                    }
                                                }
                                            }
                                            is GetTransactionsUseCase.GetTransactionsResult.Error -> {
                                                _categoriesState.value = CategoryState.Empty
                                            }
                                        }
                                    }
                                CategoryState.Loading // Temporary state while processing
                            }
                        }
                    }
                    is GetCategoriesUseCase.GetCategoriesResult.Error -> CategoryState.Empty
                }
            }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Filter Management
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Public Actions
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun setTransactionFilter(filter: TransactionFilter) {
        currentFilter = filter
    }

    fun clearDateFilter() {
        _selectedStartDate = ""
        _selectedEndDate = ""
        refreshData()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Helper Functions
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun setEmptyStates() {
        _walletsState.value = WalletState.Empty
        _transactionsState.value = TransactionState.Empty
        _categoriesState.value = CategoryState.Empty
    }
}

/**
 * Enum representing different time period filters for transactions.
 */
enum class TransactionFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\