package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeMainViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "HomeMainViewModel"
    }

    private val _wallets = MutableLiveData<List<Wallet>>()
    val wallets: LiveData<List<Wallet>> = _wallets

    private val _selectedTransaction = MutableLiveData<Transaction>()
    val selectedTransaction: LiveData<Transaction> = _selectedTransaction

    fun loadWallets() {
        Log.d(TAG, "loadWallets() called")
        viewModelScope.launch {
            try {
                val userId = getUserIdUseCase.execute()
                Log.d(TAG, "Retrieved userId: $userId")

                val walletList = getWalletUseCase.execute(userId)
                Log.d(TAG, "Fetched ${walletList.size} wallets for user")

                _wallets.value = walletList
            } catch (e: Exception) {
                Log.e(TAG, "Error loading wallets: ${e.message}", e)
            }
        }
    }

    fun setTransaction(transaction: Transaction) {
        _selectedTransaction.value = transaction
    }
}
