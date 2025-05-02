package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class WalletMainViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase
) : ViewModel() {

    private val _wallets = MutableLiveData<List<Wallet>>()
    val wallets: LiveData<List<Wallet>> = _wallets

    fun fetchWallets(userId: Int) {
        viewModelScope.launch {
            val result = getWalletUseCase.execute(userId)
            _wallets.postValue(result)
        }
    }
}