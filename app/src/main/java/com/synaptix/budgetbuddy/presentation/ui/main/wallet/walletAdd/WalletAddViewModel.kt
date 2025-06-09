package com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletAdd

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.AddWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
        val isWalletMinGoalValid: Boolean = false,
        val isWalletMaxGoalValid: Boolean = false,
        val walletNameError: String? = null,
        val walletCurrencyError: String? = null,
        val walletAmountError: String? = null,
        val walletMinGoalError: String? = null,
        val walletMaxGoalError: String? = null,
        val shouldShowErrors: Boolean = false
    )

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _validationState = MutableStateFlow(ValidationState())
    val validationState: StateFlow<ValidationState> = _validationState

    // Form fields
    private val _walletName = MutableStateFlow("")
    val walletName: StateFlow<String> = _walletName

    private val _walletCurrency = MutableStateFlow("")
    val walletCurrency: StateFlow<String> = _walletCurrency

    private val _walletAmount = MutableStateFlow(0.0)
    val walletAmount: StateFlow<Double> = _walletAmount

    private val _walletMinGoal = MutableStateFlow(0.0)
    val walletMinGoal: StateFlow<Double> = _walletMinGoal

    private val _walletMaxGoal = MutableStateFlow(0.0)
    val walletMaxGoal: StateFlow<Double> = _walletMaxGoal

    private val _enableNotifications = MutableStateFlow(true)
    val enableNotifications: StateFlow<Boolean> = _enableNotifications

    private val _excludeFromTotal = MutableStateFlow(false)
    val excludeFromTotal: StateFlow<Boolean> = _excludeFromTotal

    fun setWalletName(name: String) {
        _walletName.value = name
        validateForm()
    }

    fun setWalletCurrency(currency: String) {
        _walletCurrency.value = currency
        validateForm()
    }

    fun setWalletAmount(amount: Double) {
        _walletAmount.value = amount
        validateForm()
    }

    fun setWalletMinGoal(minGoal: Double) {
        _walletMinGoal.value = minGoal
        validateForm()
    }

    fun setWalletMaxGoal(maxGoal: Double) {
        _walletMaxGoal.value = maxGoal
        validateForm()
    }

    fun updateExcludeFromTotal(exclude: Boolean) {
        _excludeFromTotal.value = exclude
    }

    // Validation Functions
    fun validateForm() : Boolean {
        val name = _walletName.value
        val currency = _walletCurrency.value
        val amount = _walletAmount.value
        val minGoal = _walletMinGoal.value
        val maxGoal = _walletMaxGoal.value

        val (isWalletNameValid, walletNameError) = validateWalletName(name)
        val (isWalletCurrencyValid, walletCurrencyError) = validateWalletCurrency(currency)
        val (isWalletAmountValid, walletAmountError) = validateWalletAmount(amount)
        val (isWalletMinGoalValid, walletMinGoalError) = validateWalletMinGoal(minGoal)
        val (isWalletMaxGoalValid, walletMaxGoalError) = validateWalletMaxGoal(maxGoal)


        _validationState.value = _validationState.value.copy(
            isWalletNameValid = isWalletNameValid,
            isWalletCurrencyValid = isWalletCurrencyValid,
            isWalletAmountValid = isWalletAmountValid,
            isWalletMinGoalValid = isWalletMinGoalValid,
            isWalletMaxGoalValid = isWalletMaxGoalValid,
            walletNameError = walletNameError,
            walletCurrencyError = walletCurrencyError,
            walletAmountError = walletAmountError,
            walletMinGoalError = walletMinGoalError,
            walletMaxGoalError = walletMaxGoalError
        )

        return isWalletNameValid && isWalletCurrencyValid && isWalletAmountValid && isWalletMinGoalValid && isWalletMaxGoalValid
    }

    private fun validateWalletName(name: String) : Pair<Boolean, String?> {
        val isValid = name.isNotEmpty()
        val error = if (isValid) null else "Wallet name cannot be empty"
        return Pair(isValid, error)
    }

    private fun validateWalletCurrency(currency: String) : Pair<Boolean, String?> {
        val isValid = currency.isNotEmpty()
        val error = if (isValid) null else "Wallet currency cannot be empty"
        return Pair(isValid, error)
    }

    private fun validateWalletAmount(amount: Double) : Pair<Boolean, String?> {
        val isValid = amount > 0
        val error = if (isValid) null else "Wallet amount must be greater than 0"
        return Pair(isValid, error)
    }

    private fun validateWalletMinGoal(minGoal: Double) : Pair<Boolean, String?> {
        val isValid = minGoal >= 0
        val error = if (isValid) null else "Minimum goal must be 0 or greater"
        return Pair(isValid, error)
    }

    private fun validateWalletMaxGoal(maxGoal: Double) : Pair<Boolean, String?> {
        val isValid = maxGoal >= 0
        val error = if (isValid) null else "Maximum goal must be 0 or greater"
        return Pair(isValid, error)
    }

    fun showValidationErrors() {
        _validationState.value = _validationState.value.copy(shouldShowErrors = true)
        validateForm()
    }


    // Save Function
    fun addWallet() {
        if (!validateForm()) {
            showValidationErrors()
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val userId = getUserIdUseCase.execute()
                if (userId.isEmpty()) {
                    _uiState.value = UiState.Error("User ID is empty")
                    return@launch
                }

                val tempUser = User(userId, "", "", "")
                val newWallet = Wallet.new(
                    user = tempUser,
                    name = _walletName.value,
                    currency = _walletCurrency.value,
                    balance = _walletAmount.value,
                    minGoal = _walletMinGoal.value,
                    maxGoal = _walletMaxGoal.value,
                    excludeFromTotal = _excludeFromTotal.value
                )

                addWalletUseCase.execute(newWallet)
                    .catch { e ->
                        Log.e("WalletAddViewModel", "Error in wallet creation flow: ${e.message}")
                        _uiState.value = UiState.Error(e.message ?: "Failed to add wallet")
                    }
                    .collect { result ->
                        when (result) {
                            is AddWalletUseCase.AddWalletResult.Success -> {
                                Log.d("WalletAddViewModel", "Wallet added successfully: ${result.walletId}")
                                _uiState.value = UiState.Success
                            }
                            is AddWalletUseCase.AddWalletResult.Error -> {
                                Log.e("WalletAddViewModel", "Error adding wallet: ${result.message}")
                                _uiState.value = UiState.Error(result.message)
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("WalletAddViewModel", "Exception adding wallet: ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Failed to add wallet")
            }
        }
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Reset
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun reset() {
        _walletName.value = ""
        _walletCurrency.value = "ZAR"
        _walletAmount.value = 0.0
        _walletMinGoal.value = 0.0
        _walletMaxGoal.value = 0.0
        _enableNotifications.value = true
        _excludeFromTotal.value = false
        _validationState.value = ValidationState(shouldShowErrors = false)
        _uiState.value = UiState.Initial
    }
}