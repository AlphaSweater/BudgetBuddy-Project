package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletMainViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    private val _wallets = MutableLiveData<List<Wallet>>()
    val wallets: LiveData<List<Wallet>> = _wallets

    private val _totalBalance = MutableLiveData<Double>()
    val totalBalance: LiveData<Double> = _totalBalance

    private val _isBalanceVisible = MutableLiveData(true)
    val isBalanceVisible: LiveData<Boolean> = _isBalanceVisible

    fun fetchWallets() {
        viewModelScope.launch {
            try {
                val userId = getUserIdUseCase.execute()
                when (val result = getWalletUseCase.execute(userId)) {
                    is GetWalletUseCase.GetWalletResult.Success -> {
                        val walletsList = result.wallets
                        _wallets.value = walletsList
                        Log.d("WalletMainViewModel", "Fetched wallets: $walletsList")
                        calculateTotalBalance(walletsList)
                    }
                    is GetWalletUseCase.GetWalletResult.Error -> {
                        Log.e("WalletMainViewModel", "Error fetching wallets: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                // TODO: Handle error state
                _wallets.value = emptyList()
                _totalBalance.value = 0.0
            }
        }
    }

    private fun calculateTotalBalance(wallets: List<Wallet>) {
        // Calculate the total balance of all wallets that are not excluded from the total
        val total = wallets.filter { !it.excludeFromTotal }
            .sumOf { it.balance }
        _totalBalance.value = total
    }

    fun toggleBalanceVisibility() {
        _isBalanceVisible.value = _isBalanceVisible.value?.not() != false
    }
}