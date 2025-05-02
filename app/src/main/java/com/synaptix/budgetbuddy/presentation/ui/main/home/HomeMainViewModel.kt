package com.synaptix.budgetbuddy.presentation.ui.main.home

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
    class HomeMainViewModel @Inject constructor(
        private val getWalletUseCase: GetWalletUseCase,
        private val getUserIdUseCase: GetUserIdUseCase
    ) : ViewModel() {

        private val _wallets = MutableLiveData<List<Wallet>>()
        val wallets: LiveData<List<Wallet>> = _wallets

        fun loadWallets() {
            viewModelScope.launch {
                val userId = getUserIdUseCase.execute()
                val walletList = getWalletUseCase.execute(userId)
                _wallets.value = walletList
            }
        }
    }
