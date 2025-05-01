package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.walletSelectorPopUpBottomSheet

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
class WalletSelectorBottomSheetViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    private val _wallets = MutableLiveData<List<Wallet>>()
    val wallets: LiveData<List<Wallet>> = _wallets

    //AI assisted with logic for this function
    // This function loads the wallets for the user based on userID and updates the LiveData
    fun loadWallets() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            val walletEntities = getWalletUseCase.execute(userId)

            //maps out the walletEntities to a list of Wallet objects
            _wallets.value = walletEntities.map {
                Wallet(
                    it.wallet_id,
                    it.user_id,
                    it.name,
                    it.currency,
                    it.balance
                )
            }
        }
    }
}