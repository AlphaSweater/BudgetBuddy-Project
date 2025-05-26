package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectWalletPopUp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import com.synaptix.budgetbuddy.data.firebase.model.WalletDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionSelectWalletViewModel @Inject constructor(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getWalletUseCase: GetWalletUseCase
) : ViewModel() {

    private val _wallets = MutableLiveData<List<Wallet>>()
    val wallets: LiveData<List<Wallet>>
        get() = _wallets

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun loadWallets() {
        viewModelScope.launch {
            try {
                val userId = getUserIdUseCase.execute()
                when (val result = getWalletUseCase.execute(userId)) {
                    is GetWalletUseCase.GetWalletResult.Success -> {
                        _wallets.value = result.wallets
                    }
                    is GetWalletUseCase.GetWalletResult.Error -> {
                        _error.value = result.message
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load wallets"
            }
        }
    }
}