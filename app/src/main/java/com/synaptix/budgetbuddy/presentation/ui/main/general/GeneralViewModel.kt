package com.synaptix.budgetbuddy.presentation.ui.main.general

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoriesUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.label.GetLabelsUseCase
import com.synaptix.budgetbuddy.core.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel for the General screens (Reports and Transactions).
 * 
 * This ViewModel is responsible for:
 * 1. Managing the UI state for transactions, categories, and wallets
 * 2. Loading and filtering data from the backend
 * 3. Handling date range filtering
 * 4. Managing expense goals
 * 5. Persisting user selections (wallet and date range)
 * 
 * The ViewModel uses:
 * - StateFlow for UI state management
 * - Coroutines for asynchronous operations
 * - UseCases for business logic
 * - SavedStateHandle for state persistence
 * 
 * Data Flow:
 * Repository -> UseCase -> ViewModel -> UI
 */
@HiltViewModel
class GeneralViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getWalletsUseCase: GetWalletsUseCase,
    private val getLabelsUseCase: GetLabelsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Constants
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    companion object {
        private const val KEY_SELECTED_WALLET_ID = "selected_wallet_id"
        private const val KEY_DATE_RANGE_START = "date_range_start"
        private const val KEY_DATE_RANGE_END = "date_range_end"
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // State Definitions
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
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

    sealed class WalletState {
        object Loading : WalletState()
        data class Success(val wallets: List<Wallet>) : WalletState()
        data class Error(val message: String) : WalletState()
        object Empty : WalletState()
    }

    sealed class LabelState {
        object Loading : LabelState()
        data class Success(val labels: List<Label>) : LabelState()
        data class Error(val message: String) : LabelState()
        object Empty : LabelState()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // UI State
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private var _allTransactions = emptyList<Transaction>()
    private var _filteredTransactions = emptyList<Transaction>()

    private val _transactionsState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionsState: StateFlow<TransactionState> = _transactionsState

    private val _categoriesState = MutableStateFlow<CategoryState>(CategoryState.Loading)
    val categoriesState: StateFlow<CategoryState> = _categoriesState

    private val _walletState = MutableStateFlow<WalletState>(WalletState.Loading)
    val walletState: StateFlow<WalletState> = _walletState

    private val _selectedWallet = MutableStateFlow<Wallet?>(null)
    val selectedWallet: StateFlow<Wallet?> = _selectedWallet.asStateFlow()

    private val _selectedWallets = MutableStateFlow<List<Wallet>>(emptyList())
    val selectedWallets: StateFlow<List<Wallet>> = _selectedWallets.asStateFlow()

    private val _dateRange = MutableStateFlow<ClosedRange<Long>?>(null)
    val dateRange: StateFlow<ClosedRange<Long>?> = _dateRange.asStateFlow()

    private val _expenseGoal = MutableStateFlow<Pair<Double, Double>?>(null)
    val expenseGoal: StateFlow<Pair<Double, Double>?> = _expenseGoal.asStateFlow()

    private var _totalBalance = MutableStateFlow<Double>(0.0)
    var totalBalance: StateFlow<Double> = _totalBalance.asStateFlow()

    private val _labelsState = MutableStateFlow<LabelState>(LabelState.Loading)
    val labelsState: StateFlow<LabelState> = _labelsState

    fun setTotalBalance(balance: Double) {
        _totalBalance.value = balance
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Initialization
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    init {
        // Restore saved state
        restoreSavedState()
        loadData()
        
        // Handle wallet ID and startDestination from bundle
        viewModelScope.launch {
            // Get startDestination from bundle
            val startDestination = savedStateHandle.get<String>("startDestination") ?: "generalReportsFragment"
            
            // Handle wallet ID from bundle if present
            savedStateHandle.get<String>("walletId")?.let { walletId ->
                // Wait for wallets to load before setting the selected wallet
                walletState.collect { state ->
                    if (state is WalletState.Success) {
                        val wallet = state.wallets.find { it.id == walletId }
                        if (wallet != null) {
                            selectWallet(wallet)
                        } else {
                            // If wallet not found, select all wallets
                            selectWallet(null)
                        }
                    }
                }
            }
        }
        
        // Set up goal observation
        viewModelScope.launch {
            combine(_selectedWallets, _walletState) { selectedWallets, walletState ->
                when (walletState) {
                    is WalletState.Success -> {
                        if (selectedWallets.isEmpty()) {
                            // All wallets selected - sum up all min and max goals
                            // Only include non-excluded wallets in the total
                            val includedWallets = walletState.wallets.filter { !it.excludeFromTotal }
                            val totalMinGoal = includedWallets.sumOf { it.minGoal }
                            val totalMaxGoal = includedWallets.sumOf { it.maxGoal }
                            Log.d("ExpenseGoal", "All wallets selected - Total Min: $totalMinGoal, Max: $totalMaxGoal")
                            totalMinGoal to totalMaxGoal
                        } else {
                            // Specific wallets selected
                            val totalMinGoal = selectedWallets.sumOf { it.minGoal }
                            val totalMaxGoal = selectedWallets.sumOf { it.maxGoal }
                            Log.d("ExpenseGoal", "Selected wallets - Total Min: $totalMinGoal, Max: $totalMaxGoal")
                            totalMinGoal to totalMaxGoal
                        }
                    }
                    else -> {
                        Log.d("ExpenseGoal", "No wallet state available")
                        null
                    }
                }
            }.collect { goals ->
                _expenseGoal.value = goals
            }
        }
    }

    private fun restoreSavedState() {
        // Restore selected wallet
        savedStateHandle.get<String>(KEY_SELECTED_WALLET_ID)?.let { walletId ->
            viewModelScope.launch {
                // Wait for wallets to load before setting the selected wallet
                walletState.collect { state ->
                    if (state is WalletState.Success) {
                        val wallet = state.wallets.find { it.id == walletId }
                        if (wallet != null) {
                            _selectedWallet.value = wallet
                        }
                    }
                }
            }
        }

        // Restore date range
        val startDate = savedStateHandle.get<Long>(KEY_DATE_RANGE_START)
        val endDate = savedStateHandle.get<Long>(KEY_DATE_RANGE_END)
        if (startDate != null && endDate != null) {
            _dateRange.value = startDate..endDate
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Data Management
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun loadData() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                setEmptyStates()
                return@launch
            }

            launch { loadTransactions(userId) }
            launch { loadCategories(userId) }
            launch { loadWallets(userId) }
            launch { loadLabels(userId) }
        }
    }

    private suspend fun loadTransactions(userId: String) {
        getTransactionsUseCase.execute(userId)
            .catch { e ->
                _transactionsState.value = TransactionState.Error(e.message ?: "Unknown error")
            }
            .collect { result ->
                when (result) {
                    is GetTransactionsUseCase.GetTransactionsResult.Success -> {
                        _allTransactions = result.transactions
                        filterTransactions()
                    }
                    is GetTransactionsUseCase.GetTransactionsResult.Error -> {
                        _transactionsState.value = TransactionState.Error("Failed to load transactions")
                    }
                }
            }
    }

    private suspend fun loadCategories(userId: String) {
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

    private suspend fun loadWallets(userId: String) {
        getWalletsUseCase.execute(userId)
            .catch { e ->
                Log.e("WalletLoad", "Error loading wallets", e)
                _walletState.value = WalletState.Error(e.message ?: "Failed to load wallets")
            }
            .collect { result ->
                _walletState.value = when (result) {
                    is GetWalletsUseCase.GetWalletResult.Success -> {
                        WalletState.Success(result.wallets)
                    }
                    is GetWalletsUseCase.GetWalletResult.Error -> {
                        WalletState.Error("Failed to load wallets")
                    }
                }
            }
    }

    private suspend fun loadLabels(userId: String) {
        getLabelsUseCase.execute(userId)
            .catch { e ->
                _labelsState.value = LabelState.Error(e.message ?: "Unknown error")
            }
            .collect { result ->
                _labelsState.value = when (result) {
                    is GetLabelsUseCase.GetLabelsResult.Success -> {
                        if (result.labels.isEmpty()) LabelState.Empty
                        else LabelState.Success(result.labels)
                    }
                    is GetLabelsUseCase.GetLabelsResult.Error ->
                        LabelState.Error("Failed to load labels")
                }
            }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Filter Management
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun filterTransactions() {
        val dateRange = _dateRange.value
        val selectedWallet = _selectedWallet.value

        _filteredTransactions = _allTransactions.filter { transaction ->
            // Date range filter
            val isInDateRange = if (dateRange != null) {
                // Convert transaction date to start of day for comparison
                val cal = Calendar.getInstance().apply {
                    timeInMillis = transaction.date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val transactionDay = cal.timeInMillis
                transactionDay in dateRange.start..dateRange.endInclusive
            } else {
                // Default to current month if no date range is set
                val (startDate, endDate) = DateUtil.getCurrentMonthRange()
                transaction.date in startDate..endDate
            }

            // Wallet filter - if selectedWallet is null, show all wallets
            val isInSelectedWallet = selectedWallet?.let { wallet ->
                transaction.wallet.id == wallet.id
            } ?: true // If no wallet is selected (null), include all transactions

            isInDateRange && isInSelectedWallet
        }

        Log.d("Filtering", "Filtered transactions: ${_filteredTransactions.size} items")
        Log.d("Filtering", "Income transactions: ${_filteredTransactions.count { it.category.type.equals("income", true) }}")
        Log.d("Filtering", "Expense transactions: ${_filteredTransactions.count { !it.category.type.equals("income", true) }}")
        Log.d("Filtering", "Date range: ${dateRange?.let { "${Date(it.start)} - ${Date(it.endInclusive)}" } ?: "null"}")
        Log.d("Filtering", "Selected wallet: ${selectedWallet?.name ?: "All Wallets"}")

        _transactionsState.value = TransactionState.Success(_filteredTransactions)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Public Actions
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun getTransactionsByType(type: String): List<Transaction> {
        return _filteredTransactions.filter { transaction ->
            if (type.equals("income", ignoreCase = true)) {
                transaction.category.type.equals("income", ignoreCase = true)
            } else {
                !transaction.category.type.equals("income", ignoreCase = true)
            }
        }
    }

    fun getCategoriesByType(type: String): List<Category> {
        return when (val state = categoriesState.value) {
            is CategoryState.Success -> state.categories.filter {
                it.type.equals(type, ignoreCase = true)
            }
            else -> emptyList()
        }
    }

    fun selectWallet(wallet: Wallet?) {
        _selectedWallet.value = wallet
        // Update selected wallets list
        _selectedWallets.value = if (wallet == null) {
            // All wallets selected
            when (val state = _walletState.value) {
                is WalletState.Success -> state.wallets
                else -> emptyList()
            }
        } else {
            // Single wallet selected
            listOf(wallet)
        }
        // Save selected wallet ID to SavedStateHandle
        savedStateHandle[KEY_SELECTED_WALLET_ID] = wallet?.id
        filterTransactions()
    }

    fun setDateRange(startDate: Long, endDate: Long) {
        // Normalize the dates to start and end of day
        val startCal = Calendar.getInstance().apply {
            timeInMillis = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCal = Calendar.getInstance().apply {
            timeInMillis = endDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val range = startCal.timeInMillis..endCal.timeInMillis
        _dateRange.value = range
        
        // Save date range to SavedStateHandle
        savedStateHandle[KEY_DATE_RANGE_START] = range.start
        savedStateHandle[KEY_DATE_RANGE_END] = range.endInclusive
        
        filterTransactions()
    }

    fun clearDateRange() {
        _dateRange.value = null
        // Clear saved date range
        savedStateHandle.remove<Long>(KEY_DATE_RANGE_START)
        savedStateHandle.remove<Long>(KEY_DATE_RANGE_END)
        filterTransactions()
    }

    fun refresh() {
        loadData()
    }

    fun getLabelsByType(type: String): List<Label> {
        return when (val state = labelsState.value) {
            is LabelState.Success -> state.labels
            else -> emptyList()
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Helper Functions
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun setEmptyStates() {
        _transactionsState.value = TransactionState.Empty
        _categoriesState.value = CategoryState.Empty
        _walletState.value = WalletState.Empty
        _labelsState.value = LabelState.Empty
    }
} 