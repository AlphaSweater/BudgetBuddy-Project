//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.budgetSelectWalletPopUp

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
class BudgetSelectWalletViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    // LiveData holding the list of wallets
    private val _wallets = MutableLiveData<List<Wallet>>()
    val wallets: LiveData<List<Wallet>> = _wallets

    /**
     * Loads wallets for the currently authenticated user.
     */
    fun loadWallets() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            when (val result = getWalletUseCase.execute(userId)) {
                is GetWalletUseCase.GetWalletResult.Success -> {
                    _wallets.value = result.wallets
                }
                is GetWalletUseCase.GetWalletResult.Error -> {
                    Log.e("BudgetSelectWalletViewModel", "Failed to load wallets: ${result.message}")
                }
            }
        }
    }
}
