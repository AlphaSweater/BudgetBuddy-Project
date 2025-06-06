package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import android.util.Log
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the General Reports screen.
 * 
 * This ViewModel is responsible for:
 * 1. Managing the UI state for transactions and categories
 * 2. Loading data from the backend
 * 3. Transforming data for the UI
 * 4. Handling errors and empty states
 * 
 * The ViewModel uses Kotlin Flows for reactive programming:
 * - StateFlow for UI state management
 * - Flow for data streams from the backend
 * 
 * Data Flow:
 * Repository -> UseCase -> ViewModel -> UI
 * 
 * Key Concepts:
 * 1. StateFlow:
 *    - A hot flow that maintains the current state
 *    - Emits updates to all collectors when the state changes
 *    - Unlike LiveData, it's designed for coroutines
 *    - Example: _transactionsState emits updates when transaction data changes
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
class GeneralReportsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getWalletsUseCase: GetWalletUseCase
) : ViewModel() {

    /**
     * Sealed class representing the possible states for transaction data.
     * This helps in handling different UI states and error cases.
     */
    sealed class TransactionState {
        object Loading : TransactionState()
        data class Success(val transactions: List<Transaction>) : TransactionState()
        data class Error(val message: String) : TransactionState()
        object Empty : TransactionState()
    }

    /**
     * Sealed class representing the possible states for category data.
     * Similar to TransactionState, this helps in managing UI states for categories.
     */
    sealed class CategoryState {
        object Loading : CategoryState()
        data class Success(val categories: List<Category>) : CategoryState()
        data class Error(val message: String) : CategoryState()
        object Empty : CategoryState()
    }

    /**
     * StateFlow for transaction data.
     * This is a hot flow that maintains the current state and emits updates to collectors.
     * The UI observes this to update the transaction-related views.
     */
    private val _transactionsState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionsState: StateFlow<TransactionState> = _transactionsState

    /**
     * StateFlow for category data.
     * Similar to transactionsState, this maintains the current state of categories.
     * The UI observes this to update category-related views.
     */
    private val _categoriesState = MutableStateFlow<CategoryState>(CategoryState.Loading)
    val categoriesState: StateFlow<CategoryState> = _categoriesState

    init {
        // Load data when the ViewModel is created
        loadData()
    }

    private val _walletState = MutableStateFlow<List<Wallet>>(emptyList())
    val walletState: StateFlow<List<Wallet>> = _walletState


    private val _selectedWallet = MutableStateFlow<Wallet?>(null)
    val selectedWallet: StateFlow<Wallet?> = _selectedWallet.asStateFlow()
    /**
     * Loads all required data for the reports screen.
     * 
     * Data Flow Process:
     * 1. Get User ID:
     *    - Synchronously get the current user's ID
     *    - If no user ID, set all states to Empty
     * 
     * 2. Parallel Data Loading:
     *    - Launch separate coroutines for transactions and categories
     *    - Each coroutine runs independently
     *    - Both coroutines run in parallel for better performance
     * 
     * 3. Flow Collection:
     *    - Each UseCase returns a Flow
     *    - collect() starts collecting values from the Flow
     *    - catch() handles any errors in the Flow
     *    - State is updated based on the result
     * 
     * 4. Error Handling:
     *    - Each Flow has its own error handling
     *    - Errors are caught and converted to Error state
     *    - UI can handle Error state appropriately
     * 
     * Example Flow:
     * Repository (Firebase) -> UseCase (Flow) -> ViewModel (StateFlow) -> UI (collect)
     */
    fun loadData() {
        viewModelScope.launch {
            // Get user ID synchronously
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _transactionsState.value = TransactionState.Empty
                _categoriesState.value = CategoryState.Empty
                return@launch
            }

            // Launch parallel coroutines for each data type
            launch {
                // Transaction Flow Collection
                getTransactionsUseCase.execute(userId)
                    .catch { e ->
                        // Handle errors by setting Error state
                        _transactionsState.value = TransactionState.Error(e.message ?: "Unknown error")
                    }
                    .collect { result ->
                        // Process successful result
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

            launch {
                // Category Flow Collection
                getCategoriesUseCase.execute(userId)
                    .catch { e ->
                        // Handle errors by setting Error state
                        _categoriesState.value = CategoryState.Error(e.message ?: "Unknown error")
                    }
                    .collect { result ->
                        // Process successful result
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
            launch {
                getWalletsUseCase.execute(userId)
                    .catch { e ->
                        // You can log this or create a WalletState sealed class if needed
                    }
                    .collect { result ->
                        when (result) {
                            is GetWalletUseCase.GetWalletResult.Success -> {
                                _walletState.value = result.wallets
                            }
                            is GetWalletUseCase.GetWalletResult.Error -> {
                                // Log or ignore, based on your needs
                            }
                        }
                    }
            }

        }
    }

    /**
     * Gets transactions filtered by type (income/expense).
     * This is a helper function used by the UI to get filtered data.
     * 
     * @param type The type of transactions to filter ("income" or "expense")
     * @return List of filtered transactions
     */
    fun getTransactionsByType(type: String): List<Transaction> {
        return when (val state = transactionsState.value) {
            is TransactionState.Success -> state.transactions.filter {
                it.category.type.equals(type, ignoreCase = true) &&
                        (selectedWallet.value == null || it.wallet.id == selectedWallet.value?.id)
            }
            else -> emptyList()
        }
    }

    /**
     * Gets categories filtered by type (income/expense).
     * This is a helper function used by the UI to get filtered data.
     * 
     * @param type The type of categories to filter ("income" or "expense")
     * @return List of filtered categories
     */
    fun getCategoriesByType(type: String): List<Category> {
        return when (val state = categoriesState.value) {
            is CategoryState.Success -> state.categories.filter { 
                it.type.equals(type, ignoreCase = true) 
            }
            else -> emptyList()
        }
    }


    fun selectWallet(wallet: Wallet) {
        Log.d("WalletDropdown", "ViewModel selectWallet called with: ${wallet.name}")
        _selectedWallet.value = wallet
    }


}