package com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletAdd

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.AddWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletAddViewModel @Inject constructor(
    private val addWalletUseCase: AddWalletUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    // UI State
    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    data class ValidationState(
        val isWalletNameValid: Boolean = false,
        val isWalletCurrencyValid: Boolean = false,
        val isWalletAmountValid: Boolean = false,
        val walletNameError: String? = null,
        val walletCurrencyError: String? = null,
        val walletAmountError: String? = null,
        val shouldShowErrors: Boolean = false
    )

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _validationState = MutableLiveData(ValidationState())
    val validationState: LiveData<ValidationState> = _validationState

    // Form fields
    private val _walletName = MutableLiveData<String>()
    val walletName: LiveData<String> = _walletName

    private val _walletCurrency = MutableLiveData<String>()
    val walletCurrency: LiveData<String> = _walletCurrency

    private val _walletAmount = MutableLiveData<Double>()
    val walletAmount: LiveData<Double> = _walletAmount

    private val _enableNotifications = MutableLiveData(true)
    val enableNotifications: LiveData<Boolean> = _enableNotifications

    private val _excludeFromTotal = MutableLiveData(false)
    val excludeFromTotal: LiveData<Boolean> = _excludeFromTotal

    fun setWalletName(name: String) {
        _walletName.value = name
        validateForm()
    }

    fun setWalletCurrency(currency: String) {
        _walletCurrency.value = currency
        validateForm()
    }

    fun setWalletAmount(amount: String) {
        _walletAmount.value = amount.toDoubleOrNull()
        validateForm()
    }

    fun updateEnableNotifications(enabled: Boolean) {
        _enableNotifications.value = enabled
    }

    fun updateExcludeFromTotal(exclude: Boolean) {
        _excludeFromTotal.value = exclude
    }

    // Validation Functions
    fun validateForm() : Boolean {
        val name = _walletName.value
        val currency = _walletCurrency.value
        val amount = _walletAmount.value

        val (isWalletNameValid, walletNameError) = validateWalletName(name)
        val (isWalletCurrencyValid, walletCurrencyError) = validateWalletCurrency(currency)
        val (isWalletAmountValid, walletAmountError) = validateWalletAmount(amount)

        val currentState = _validationState.value ?: ValidationState()
        _validationState.value = currentState.copy(
            isWalletNameValid = isWalletNameValid,
            isWalletCurrencyValid = isWalletCurrencyValid,
            isWalletAmountValid = isWalletAmountValid,
            walletNameError = walletNameError,
            walletCurrencyError = walletCurrencyError,
            walletAmountError = walletAmountError
        )

        return isWalletNameValid && isWalletCurrencyValid && isWalletAmountValid
    }

    private fun validateWalletName(name: String?) : Pair<Boolean, String?> {
        val isValid = !name.isNullOrEmpty()
        val error = if (isValid) null else "Wallet name cannot be empty"

        return Pair(isValid, error)
    }

    private fun validateWalletCurrency(currency: String?) : Pair<Boolean, String?> {
        val isValid = !currency.isNullOrEmpty()
        val error = if (isValid) null else "Wallet currency cannot be empty"

        return Pair(isValid, error)
    }

    private fun validateWalletAmount(amount: Double?) : Pair<Boolean, String?> {
        val isValid = amount != null && amount > 0
        val error = if (isValid) null else "Wallet amount must be greater than 0"

        return Pair(isValid, error)
    }

    fun showValidationErrors() {
        val currentState = _validationState.value ?: ValidationState()
        _validationState.value = currentState.copy(shouldShowErrors = true)
        validateForm()
    }

    // Save Function
    fun addWallet() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val userId = getUserIdUseCase.execute()
                val tempUser = User(userId, "", "", "")
                val newWallet = Wallet.new(
                    user = tempUser,
                    name = _walletName.value ?: "",
                    currency = _walletCurrency.value ?: "",
                    balance = _walletAmount.value ?: 0.0,
                    excludeFromTotal = _excludeFromTotal.value == true
                )

                when (val result = addWalletUseCase.execute(newWallet)) {
                    is AddWalletUseCase.AddWalletResult.Success -> {
                        Log.d("WalletAddViewModel", "Wallet added successfully: ${result.walletId}")
                        _uiState.value = UiState.Success
                    }
                    is AddWalletUseCase.AddWalletResult.Error -> {
                        Log.e("WalletAddViewModel", "Error adding wallet: ${result.message}")
                        _uiState.value = UiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e("WalletAddViewModel", "Exception adding wallet: ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Failed to add wallet")
            }
        }
    }

    fun reset() {
        _walletName.value = ""
        _walletCurrency.value = "ZAR"
        _walletAmount.value = 0.0
        _enableNotifications.value = true
        _excludeFromTotal.value = false
        _validationState.value = ValidationState(shouldShowErrors = false)
        _uiState.value = UiState.Initial
    }
}