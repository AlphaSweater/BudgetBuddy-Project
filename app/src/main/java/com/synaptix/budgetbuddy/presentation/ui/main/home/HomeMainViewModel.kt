package com.synaptix.budgetbuddy.presentation.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoriesUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import com.synaptix.budgetbuddy.data.firebase.model.TransactionDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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
    private val getWalletUseCase: GetWalletUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    /**
     * LiveData for pie chart entries.
     * Used to update the pie chart when data changes.
     */
    private val _pieEntries = MutableLiveData<List<PieEntry>>()
    val pieEntries: LiveData<List<PieEntry>> = _pieEntries

    /**
     * Sealed class representing the possible states for wallet data.
     * This helps in handling different UI states and error cases.
     */
    sealed class WalletState {
        object Loading : WalletState()
        data class Success(val wallets: List<Wallet>) : WalletState()
        data class Error(val message: String) : WalletState()
        object Empty : WalletState()
    }

    /**
     * Sealed class representing the possible states for transaction data.
     * Similar to WalletState, this helps in managing UI states for transactions.
     */
    sealed class TransactionState {
        object Loading : TransactionState()
        data class Success(val transactions: List<Transaction>) : TransactionState()
        data class Error(val message: String) : TransactionState()
        object Empty : TransactionState()
    }

    /**
     * Sealed class representing the possible states for category data.
     * Similar to WalletState, this helps in managing UI states for categories.
     */
    sealed class CategoryState {
        object Loading : CategoryState()
        data class Success(val categories: List<Category>) : CategoryState()
        data class Error(val message: String) : CategoryState()
        object Empty : CategoryState()
    }

    /**
     * StateFlow for wallet data.
     * This is a hot flow that maintains the current state and emits updates to collectors.
     */
    private val _walletsState = MutableStateFlow<WalletState>(WalletState.Loading)
    val walletsState: StateFlow<WalletState> = _walletsState

    /**
     * StateFlow for transaction data.
     * Similar to walletsState, this maintains the current state of transactions.
     */
    private val _transactionsState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionsState: StateFlow<TransactionState> = _transactionsState

    /**
     * StateFlow for category data.
     * Similar to walletsState, this maintains the current state of categories.
     */
    private val _categoriesState = MutableStateFlow<CategoryState>(CategoryState.Loading)
    val categoriesState: StateFlow<CategoryState> = _categoriesState

    /**
     * StateFlow for total wallet balance.
     * Used to display the total balance across all wallets.
     */
    private val _totalWalletBalance = MutableStateFlow<Double?>(null)
    val totalWalletBalance: StateFlow<Double?> get() = _totalWalletBalance

    /**
     * Date formatter for parsing and formatting dates.
     * Used for date range filtering.
     */
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    /**
     * Selected date range for filtering transactions.
     * These properties trigger a data refresh when changed.
     */
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

    /**
     * Current transaction filter.
     * Used to filter transactions by time period.
     */
    private var currentFilter: TransactionFilter = TransactionFilter.ALL
        set(value) {
            field = value
            refreshData()
        }

    init {
        refreshData()
    }

    /**
     * Refreshes all data for the home screen.
     * 
     * Data Flow Process:
     * 1. Get User ID:
     *    - Synchronously get the current user's ID
     *    - If no user ID, set all states to Empty
     * 
     * 2. Parallel Data Loading:
     *    - Launch separate coroutines for each data type
     *    - Each coroutine runs independently
     *    - All coroutines run in parallel for better performance
     * 
     * 3. Flow Collection:
     *    - Each UseCase returns a Flow
     *    - collect() starts collecting values from the Flow
     *    - catch() handles any errors in the Flow
     *    - State is updated based on the result
     * 
     * 4. Error Handling:
     *    - Each Flow has its own error handling
     *    - Errors are caught and converted to Empty state
     *    - UI can handle Empty state appropriately
     * 
     * Example Flow:
     * Repository (Firebase) -> UseCase (Flow) -> ViewModel (StateFlow) -> UI (collect)
     */
    fun refreshData() {
        viewModelScope.launch {
            // Get user ID synchronously
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _walletsState.value = WalletState.Empty
                _transactionsState.value = TransactionState.Empty
                _categoriesState.value = CategoryState.Empty
                return@launch
            }

            // Launch parallel coroutines for each data type
            launch {
                // Transaction Flow Collection
                val transactionsFlow = if (_selectedStartDate.isNotEmpty() && _selectedEndDate.isNotEmpty()) {
                    val startDate = dateFormat.parse(_selectedStartDate)?.time ?: 0L
                    val endDate = dateFormat.parse(_selectedEndDate)?.time ?: 0L
                    getTransactionsUseCase.executeWithDateRange(userId, startDate, endDate)
                } else {
                    getTransactionsUseCase.execute(userId)
                }

                // Collect and handle transaction data
                transactionsFlow
                    .catch { e ->
                        // Handle errors by setting Empty state
                        _transactionsState.value = TransactionState.Empty
                    }
                    .collect { result ->
                        // Process successful result
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

            launch {
                // Wallet Flow Collection
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
                // Category Flow Collection
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

    /**
     * Filters transactions based on the current filter.
     * Supports filtering by time period (ALL, TODAY, THIS_WEEK, THIS_MONTH).
     * 
     * @param transactions List of transactions to filter
     * @return Filtered list of transactions
     */
    private fun filterTransactions(transactions: List<Transaction>): List<Transaction> {
        val filtered = when (currentFilter) {
            TransactionFilter.ALL -> transactions
            TransactionFilter.TODAY -> transactions.filter { isDateInRange(it.date, Calendar.DAY_OF_YEAR) }
            TransactionFilter.THIS_WEEK -> transactions.filter { isDateInRange(it.date, Calendar.WEEK_OF_YEAR) }
            TransactionFilter.THIS_MONTH -> transactions.filter { isDateInRange(it.date, Calendar.MONTH) }
        }
        return filtered.sortedByDescending { it.date }
    }

    /**
     * Checks if a date falls within the current time period.
     * 
     * @param date The timestamp to check
     * @param calendarField The calendar field to compare (DAY_OF_YEAR, WEEK_OF_YEAR, MONTH)
     * @return True if the date is within the current period
     */
    private fun isDateInRange(date: Long, calendarField: Int): Boolean {
        val transactionDate = Calendar.getInstance().apply {
            time = Date(date)
        }
        val today = Calendar.getInstance()

        return today.get(Calendar.YEAR) == transactionDate.get(Calendar.YEAR) &&
               today.get(calendarField) == transactionDate.get(calendarField)
    }

    /**
     * Sets the current transaction filter.
     * This triggers a data refresh with the new filter.
     * 
     * @param filter The new filter to apply
     */
    fun setTransactionFilter(filter: TransactionFilter) {
        currentFilter = filter
    }

    /**
     * Clears the date range filter.
     * This triggers a data refresh with no date filtering.
     */
    fun clearDateFilter() {
        _selectedStartDate = ""
        _selectedEndDate = ""
        refreshData()
    }

    /**
     * Updates the pie chart with transaction data.
     * Groups transactions by category and creates pie entries.
     * 
     * @param transactions List of transactions to process
     * @param categoriesMap Map of category IDs to Category objects
     */
    fun updatePieChartWithTransactions(transactions: List<TransactionDTO>, categoriesMap: Map<String, Category>) {
        val transactionsGroupedByCategory = transactions.groupingBy { it.categoryId }.eachCount()

        val pieEntries = transactionsGroupedByCategory.mapNotNull { (categoryId, count) ->
            val categoryName = categoriesMap[categoryId]?.name ?: return@mapNotNull null
            PieEntry(count.toFloat(), categoryName)
        }

        _pieEntries.value = pieEntries
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