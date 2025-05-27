package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletMainViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
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

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            if (userId.isEmpty()) {
                _walletState.value = WalletState.Empty
                _totalBalance.value = 0.0
                return@launch
            }

            getWalletUseCase.execute(userId)
                .catch { e ->
                    _walletState.value = WalletState.Empty
                    _totalBalance.value = 0.0
                }
                .collect { result ->
                    when (result) {
                        is GetWalletUseCase.GetWalletResult.Success -> {
                            val walletsList = result.wallets
                            if (walletsList.isEmpty()) {
                                _walletState.value = WalletState.Empty
                            } else {
                                _walletState.value = WalletState.Success(walletsList)
                            }
                            calculateTotalBalance(walletsList)
                        }
                        is GetWalletUseCase.GetWalletResult.Error -> {
                            _walletState.value = WalletState.Empty
                            _totalBalance.value = 0.0
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
}