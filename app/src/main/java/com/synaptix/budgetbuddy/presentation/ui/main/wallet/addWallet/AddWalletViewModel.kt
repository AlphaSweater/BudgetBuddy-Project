package com.synaptix.budgetbuddy.presentation.ui.main.wallet.addWallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.AddWalletUseCase
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddWalletViewModel @Inject constructor(
    private val addWalletUseCase: AddWalletUseCase,
    private val getUserIdUseCase: GetUserIdUseCase)
    : ViewModel() {
    // This ViewModel will handle the logic for adding a new wallet

        suspend fun addWallet(walletName: String, walletBalance: Double, currencyType: String) {
            // Call the use case to add a new wallet
            val walletEntity = WalletEntity(
                wallet_id = 0,
                user_id = getUserIdUseCase.execute(),
                name = walletName,
                currency = currencyType,
                balance = walletBalance
            )
            viewModelScope.launch {
                val result = addWalletUseCase.execute(walletEntity)
            }
        }
}
