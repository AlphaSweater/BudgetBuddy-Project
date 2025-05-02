package com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletAdd

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.AddWalletUseCase
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletAddViewModel @Inject constructor(
    private val addWalletUseCase: AddWalletUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {
    val walletId = MutableLiveData<Int>()
    val walletName = MutableLiveData<String>()
    val walletCurrency = MutableLiveData<String>()
    val walletAmount = MutableLiveData<Double>()
        suspend fun addWallet() {
            val wallet = Wallet(
                userId = getUserIdUseCase.execute(),
                walletName = walletName.value ?: "",
                walletCurrency = walletCurrency.value ?: "",
                walletBalance = walletAmount.value ?: 0.0
            )
            addWalletUseCase.execute(wallet)
        }
}