package com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletAdd

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.WalletIn
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

    // Validation State
    private val _nameError = MutableLiveData<String?>()
    val nameError: LiveData<String?> = _nameError

    private val _currencyError = MutableLiveData<String?>()
    val currencyError: LiveData<String?> = _currencyError

    private val _amountError = MutableLiveData<String?>()
    val amountError: LiveData<String?> = _amountError

    // Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Update Functions
    fun updateWalletName(name: String) {
        _walletName.value = name
        validateWalletName(name)
    }

    fun updateWalletCurrency(currency: String) {
        _walletCurrency.value = currency
        validateWalletCurrency(currency)
    }

    fun updateWalletAmount(amount: String) {
        try {
            val parsedAmount = amount.toDoubleOrNull()
            _walletAmount.value = parsedAmount
            validateWalletAmount(parsedAmount)
        } catch (e: NumberFormatException) {
            _amountError.value = "Invalid amount format"
        }
    }

    fun updateNotifications(enabled: Boolean) {
        _enableNotifications.value = enabled
    }

    fun updateExcludeFromTotal(exclude: Boolean) {
        _excludeFromTotal.value = exclude
    }

    // Validation Functions
    private fun validateWalletName(name: String?) {
        _nameError.value = when {
            name.isNullOrBlank() -> "Wallet name is required"
            name.length < 3 -> "Wallet name must be at least 3 characters"
            else -> null
        }
    }

    private fun validateWalletCurrency(currency: String?) {
        _currencyError.value = when {
            currency.isNullOrBlank() -> "Currency is required"
            else -> null
        }
    }

    private fun validateWalletAmount(amount: Double?) {
        _amountError.value = when {
            amount == null -> "Amount is required"
            amount < 0 -> "Amount cannot be negative"
            else -> null
        }
    }

    private fun validateAll(): Boolean {
        validateWalletName(_walletName.value)
        validateWalletCurrency(_walletCurrency.value)
        validateWalletAmount(_walletAmount.value)

        return _nameError.value == null &&
                _currencyError.value == null &&
                _amountError.value == null
    }

    // Save Function
    suspend fun addWallet(): Result<Unit> {
        return try {
            if (!validateAll()) {
                return Result.failure(IllegalStateException("Please fix validation errors"))
            }

            _isLoading.value = true

            val wallet = WalletIn(
                userId = getUserIdUseCase.execute(),
                walletName = _walletName.value ?: "",
                walletCurrency = _walletCurrency.value ?: "",
                walletBalance = _walletAmount.value ?: 0.0,
                excludeFromTotal = _excludeFromTotal.value ?: false
            )

            addWalletUseCase.execute(wallet)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
}