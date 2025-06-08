package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectWalletPopUp

import android.util.Log
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
class TransactionSelectWalletViewModel @Inject constructor(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getWalletUseCase: GetWalletUseCase
) : ViewModel() {

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val wallets: List<Wallet>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    val wallets: StateFlow<List<Wallet>> = _wallets

    fun loadWallets() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val userId = getUserIdUseCase.execute()
                if (userId.isEmpty()) {
                    _uiState.value = UiState.Error("User ID is empty")
                    return@launch
                }

                getWalletUseCase.execute(userId)
                    .catch { e ->
                        Log.e("TransactionSelectWalletViewModel", "Error in wallets flow: ${e.message}")
                        _uiState.value = UiState.Error(e.message ?: "Failed to load wallets")
                    }
                    .collect { result ->
                        when (result) {
                            is GetWalletUseCase.GetWalletResult.Success -> {
                                Log.d("TransactionSelectWalletViewModel", "Wallets loaded successfully: ${result.wallets.size}")
                                _uiState.value = UiState.Success(result.wallets)
                                _wallets.value = result.wallets
                            }
                            is GetWalletUseCase.GetWalletResult.Error -> {
                                Log.e("TransactionSelectWalletViewModel", "Error loading wallets: ${result.message}")
                                _uiState.value = UiState.Error(result.message)
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("TransactionSelectWalletViewModel", "Exception loading wallets: ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Failed to load wallets")
            }
        }
    }
}