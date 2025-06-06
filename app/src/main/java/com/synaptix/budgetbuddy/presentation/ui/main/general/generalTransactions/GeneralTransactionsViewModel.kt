package com.synaptix.budgetbuddy.presentation.ui.main.general.generalTransactions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralTransactionsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getWalletUseCase: GetWalletUseCase
) : ViewModel() {

    sealed class TransactionState {
        object Loading : TransactionState()
        data class Success(val transactions: List<Transaction>) : TransactionState()
        data class Error(val message: String) : TransactionState()
        object Empty : TransactionState()
    }

    sealed class WalletState {
        object Loading : WalletState()
        data class Success(val wallets: List<Wallet>) : WalletState()
        data class Error(val message: String) : WalletState()
        object Empty : WalletState()
    }

    private val _transactionsState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionsState: StateFlow<TransactionState> = _transactionsState

    private val _walletsState = MutableStateFlow<WalletState>(WalletState.Loading)
    val walletsState: StateFlow<WalletState> = _walletsState

    private val _selectedWallet = MutableStateFlow<Wallet?>(null)
    val selectedWallet: StateFlow<Wallet?> = _selectedWallet.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()
    private var selectedWalletId: String? = null

    private val _dateRange = MutableStateFlow<ClosedRange<Long>?>(null)
    val dateRange: StateFlow<ClosedRange<Long>?> = _dateRange.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        loadWallets()
        loadTransactions()
    }

    private fun loadWallets() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _walletsState.value = WalletState.Error("User not logged in")
                return@launch
            }

            _walletsState.value = WalletState.Loading

            getWalletUseCase.execute(userId)
                .catch { e ->
                    _walletsState.value = WalletState.Error(
                        e.message ?: "Failed to load wallets"
                    )
                }
                .collect { result ->
                    _walletsState.value = when (result) {
                        is GetWalletUseCase.GetWalletResult.Success -> {
                            WalletState.Success(result.wallets)
                        }
                        is GetWalletUseCase.GetWalletResult.Error -> {
                            WalletState.Error("Failed to load wallets")
                        }
                    }
                }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _transactionsState.value = TransactionState.Empty
                return@launch
            }

            _transactionsState.value = TransactionState.Loading

            getTransactionsUseCase.execute(userId)
                .catch { e ->
                    _transactionsState.value = TransactionState.Error(
                        e.message ?: "Failed to load transactions"
                    )
                }
                .collect { result ->
                    when (result) {
                        is GetTransactionsUseCase.GetTransactionsResult.Success -> {
                            allTransactions = result.transactions
                            filterTransactions()
                        }
                        is GetTransactionsUseCase.GetTransactionsResult.Error -> {
                            _transactionsState.value = TransactionState.Error(result.message)
                        }
                    }
                }
        }
    }


    private fun filterTransactions() {
        val filteredByWallet = if (selectedWalletId != null) {
            allTransactions.filter { it.wallet.id == selectedWalletId }
        } else {
            allTransactions
        }

        val filteredByDate = _dateRange.value?.let { range ->
            filteredByWallet.filter { transaction ->
                transaction.date in range
            }
        } ?: filteredByWallet

        _transactionsState.value = if (filteredByDate.isEmpty()) {
            TransactionState.Empty
        } else {
            TransactionState.Success(filteredByDate.sortedByDescending { it.date })
        }
    }

    fun setDateRange(startDate: Long, endDate: Long) {
        _dateRange.value = startDate..endDate
        filterTransactions()
    }

    fun clearDateRange() {
        _dateRange.value = null
        filterTransactions()
    }

    // Update your existing selectWallet function to use the new filter
    fun selectWallet(wallet: Wallet?) {
        _selectedWallet.value = wallet
        selectedWalletId = wallet?.id
        filterTransactions()
    }

    fun refresh() {
        loadData()
    }
}