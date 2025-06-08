package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletMainViewModel @Inject constructor(
    private val getWalletsUseCase: GetWalletsUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase // Add this line
) : ViewModel() {

    sealed class WalletState {
        object Loading : WalletState()
        data class Success(val wallets: List<Wallet>) : WalletState()
        data class Error(val message: String) : WalletState()
        object Empty : WalletState()
    }

    private val _walletState = MutableStateFlow<WalletState>(WalletState.Loading)
    val walletState: StateFlow<WalletState> = _walletState

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance

    private val _isBalanceVisible = MutableStateFlow(true)
    val isBalanceVisible: StateFlow<Boolean> = _isBalanceVisible

    // Transactions state
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private var transactionsJob: Job? = null

    init {
        refreshData()
        loadTransactions() // Load transactions when ViewModel is created
    }

    fun refreshData() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _walletState.value = WalletState.Empty
                _totalBalance.value = 0.0
                return@launch
            }

            getWalletsUseCase.execute(userId)
                .catch { e ->
                    _walletState.value = WalletState.Error(e.message ?: "Unknown error occurred")
                    _totalBalance.value = 0.0
                }
                .collect { result ->
                    when (result) {
                        is GetWalletsUseCase.GetWalletResult.Success -> {
                            val walletsList = result.wallets
                            if (walletsList.isEmpty()) {
                                _walletState.value = WalletState.Empty
                            } else {
                                _walletState.value = WalletState.Success(walletsList)
                            }
                            calculateTotalBalance(walletsList)
                        }
                        is GetWalletsUseCase.GetWalletResult.Error -> {
                            _walletState.value = WalletState.Error(result.message)
                            _totalBalance.value = 0.0
                        }
                    }
                }
        }
    }

    private fun loadTransactions() {
        // Cancel any existing job to avoid multiple subscriptions
        transactionsJob?.cancel()

        transactionsJob = viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isNotEmpty()) {
                getTransactionsUseCase.execute(userId)
                    .catch { e ->
                        // Handle error, maybe log it
                        _transactions.value = emptyList()
                    }
                    .collect { result ->
                        when (result) {
                            is GetTransactionsUseCase.GetTransactionsResult.Success -> {
                                _transactions.value = result.transactions
                            }
                            is GetTransactionsUseCase.GetTransactionsResult.Error -> {
                                // Handle error, maybe log it
                                _transactions.value = emptyList()
                            }
                        }
                    }
            }
        }
    }

    private fun calculateTotalBalance(wallets: List<Wallet>) {
        val total = wallets.filter { !it.excludeFromTotal }
            .sumOf { it.balance }
        _totalBalance.value = total
    }

    fun toggleBalanceVisibility() {
        _isBalanceVisible.value = !_isBalanceVisible.value
    }

    override fun onCleared() {
        super.onCleared()
        transactionsJob?.cancel()
    }
}