package com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletReport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// WalletReportViewModel.kt
@HiltViewModel
class WalletReportViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getWalletUseCase: GetWalletUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _transactionsState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionsState: StateFlow<TransactionState> = _transactionsState

    private val _wallet = MutableStateFlow<Wallet?>(null)
    val wallet: StateFlow<Wallet?> = _wallet

    private val _dateRange = MutableStateFlow<ClosedRange<Long>?>(null)
    val dateRange: StateFlow<ClosedRange<Long>?> = _dateRange

    fun loadWalletTransactions(walletId: String) {
        viewModelScope.launch {
            _transactionsState.value = TransactionState.Loading
            println("WalletReportViewModel: Loading wallet: $walletId")

            val currentUser = auth.currentUser
            if (currentUser == null) {
                println("WalletReportViewModel: User not authenticated")
                _transactionsState.value = TransactionState.Error("User not authenticated")
                return@launch
            }

            val userId = currentUser.uid
            println("WalletReportViewModel: Using user ID: $userId")

            try {
                getWalletUseCase.execute(userId).collect { walletResult ->
                    when (walletResult) {
                        is GetWalletUseCase.GetWalletResult.Success -> {
                            println("WalletReportViewModel: Found ${walletResult.wallets.size} wallets")
                            val wallet = walletResult.wallets.find { it.id == walletId }

                            if (wallet != null) {
                                _wallet.value = wallet
                                println("WalletReportViewModel: Found wallet: ${wallet.name} (${wallet.id})")
                                loadWalletTransactionsInternal(walletId, userId)
                            } else {
                                println("WalletReportViewModel: Wallet $walletId not found")
                                _transactionsState.value = TransactionState.Error("Wallet not found")
                            }
                        }
                        is GetWalletUseCase.GetWalletResult.Error -> {
                            println("WalletReportViewModel: Error loading wallets: ${walletResult.message}")
                            _transactionsState.value = TransactionState.Error(
                                walletResult.message ?: "Error loading wallet"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                println("WalletReportViewModel: Exception: ${e.message}")
                _transactionsState.value = TransactionState.Error(
                    e.message ?: "An unknown error occurred"
                )
            }
        }
    }

    private suspend fun loadWalletTransactionsInternal(walletId: String, userId: String) {
        getTransactionsUseCase.execute(userId).collect { transactionResult ->
            when (transactionResult) {
                is GetTransactionsUseCase.GetTransactionsResult.Success -> {
                    var walletTransactions = transactionResult.transactions
                        .filter { it.wallet.id == walletId }
                        .sortedByDescending { it.date }

                    // Apply date range filter if set
                    _dateRange.value?.let { range ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = range.start
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val startOfStartDate = calendar.timeInMillis

                        calendar.timeInMillis = range.endInclusive
                        calendar.set(Calendar.HOUR_OF_DAY, 23)
                        calendar.set(Calendar.MINUTE, 59)
                        calendar.set(Calendar.SECOND, 59)
                        calendar.set(Calendar.MILLISECOND, 999)
                        val endOfEndDate = calendar.timeInMillis

                        walletTransactions = walletTransactions.filter { transaction ->
                            transaction.date in startOfStartDate..endOfEndDate
                        }
                    }

                    println("WalletReportViewModel: Found ${walletTransactions.size} transactions for wallet $walletId")

                    _transactionsState.value = if (walletTransactions.isEmpty()) {
                        TransactionState.Empty
                    } else {
                        TransactionState.Success(walletTransactions)
                    }
                }
                is GetTransactionsUseCase.GetTransactionsResult.Error -> {
                    println("WalletReportViewModel: Error loading transactions: ${transactionResult.message}")
                    _transactionsState.value = TransactionState.Error(
                        transactionResult.message ?: "Error loading transactions"
                    )
                }
            }
        }
    }
    fun setDateRange(startDate: Long, endDate: Long) {
        _dateRange.value = startDate..endDate
        wallet.value?.let { wallet ->
            loadWalletTransactions(wallet.id)
        }
    }

    fun clearDateRange() {
        _dateRange.value = null
        wallet.value?.let { wallet ->
            loadWalletTransactions(wallet.id)
        }
    }

    sealed class TransactionState {
        object Loading : TransactionState()
        data class Success(val transactions: List<Transaction>) : TransactionState()
        data class Error(val message: String) : TransactionState()
        object Empty : TransactionState()
    }
}