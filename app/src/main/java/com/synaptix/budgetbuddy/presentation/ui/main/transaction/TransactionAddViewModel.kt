package com.synaptix.budgetbuddy.presentation.ui.main.transaction

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.TransactionIn
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddTransactionUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddTransactionUseCase.AddTransactionResult
import com.synaptix.budgetbuddy.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransactionAddViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    data class ValidationState(
        val isAmountValid: Boolean = false,
        val isCurrencyValid: Boolean = false,
        val isCategoryValid: Boolean = false,
        val isWalletValid: Boolean = false,
        val isDateValid: Boolean = false,
        val amountError: String? = null,
        val currencyError: String? = null,
        val categoryError: String? = null,
        val walletError: String? = null,
        val dateError: String? = null,
        val shouldShowErrors: Boolean = false
    )

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _validationState = MutableLiveData(ValidationState())
    val validationState: LiveData<ValidationState> = _validationState

    // Form fields
    private val _category = MutableLiveData<Category?>()
    val category: LiveData<Category?> = _category

    private val _wallet = MutableLiveData<Wallet?>()
    val wallet: LiveData<Wallet?> = _wallet

    private val _currency = MutableLiveData("ZAR")
    val currency: LiveData<String> = _currency

    private val _amount = MutableLiveData<Double?>()
    val amount: LiveData<Double?> = _amount

    private val _date = MutableLiveData(getCurrentDate())
    val date: LiveData<String> = _date

    private val _note = MutableLiveData<String?>()
    val note: LiveData<String?> = _note

    private val _imageBytes = MutableLiveData<ByteArray?>()
    val imageBytes: LiveData<ByteArray?> = _imageBytes

    private val _recurrenceRate = MutableLiveData<String?>()
    val recurrenceRate: LiveData<String?> = _recurrenceRate

    var selectedLabels = MutableLiveData<List<Label>>(emptyList())

    fun setCategory(category: Category?) {
        _category.value = category
        validateForm()
    }

    fun setWallet(wallet: Wallet?) {
        _wallet.value = wallet
        validateForm()
    }

    fun setCurrency(currency: String) {
        _currency.value = currency
        validateForm()
    }

    fun setAmount(amount: String) {
        _amount.value = amount.toDoubleOrNull() ?: 0.0
        validateForm()
    }

    fun setDate(date: String) {
        _date.value = date
        validateForm()
    }

    fun setNote(note: String) {
        _note.value = note
    }

    fun setRecurrenceRate(rate: String) {
        _recurrenceRate.value = rate
    }

    fun setImageBytes(bytes: ByteArray?) {
        _imageBytes.value = bytes
    }

    fun validateForm(): Boolean {
        val isAmountValid = (_amount.value ?: 0.0) > 0.0
        val isCurrencyValid = !_currency.value.isNullOrBlank()
        val isCategoryValid = _category.value != null
        val isWalletValid = _wallet.value != null
        val isDateValid = !_date.value.isNullOrBlank()

        val currentState = _validationState.value ?: ValidationState()
        _validationState.value = currentState.copy(
            isAmountValid = isAmountValid,
            isCurrencyValid = isCurrencyValid,
            isCategoryValid = isCategoryValid,
            isWalletValid = isWalletValid,
            isDateValid = isDateValid,
            amountError = if (currentState.shouldShowErrors && !isAmountValid) "Please enter a valid amount" else null,
            currencyError = if (currentState.shouldShowErrors && !isCurrencyValid) "Please select a currency" else null,
            categoryError = if (currentState.shouldShowErrors && !isCategoryValid) "Please select a category" else null,
            walletError = if (currentState.shouldShowErrors && !isWalletValid) "Please select a wallet" else null,
            dateError = if (currentState.shouldShowErrors && !isDateValid) "Please select a date" else null
        )

        return isAmountValid && isCurrencyValid && isCategoryValid && isWalletValid && isDateValid
    }

    fun showValidationErrors() {
        val currentState = _validationState.value ?: ValidationState()
        _validationState.value = currentState.copy(shouldShowErrors = true)
        validateForm()
    }

    fun addTransaction() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val transaction = TransactionIn(
                    userId = getUserIdUseCase.execute(),
                    categoryId = _category.value?.categoryId ?: 0,
                    walletId = _wallet.value?.walletId ?: 0,
                    currencyType = _currency.value ?: "ZAR",
                    amount = _amount.value ?: 0.0,
                    date = _date.value ?: getCurrentDate(),
                    note = _note.value,
                    photo = _imageBytes.value,
                    recurrenceRate = _recurrenceRate.value
                )
                val result = addTransactionUseCase.execute(transaction)
                when (result) {
                    is AddTransactionResult.Success -> {
                        // Handle success
                        _uiState.value = UiState.Success
                        reset()
                    }
                    is AddTransactionResult.Error -> {
                        // Handle error
                        val errorMessage = result.message
                        _uiState.value = UiState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to add transaction")
            }
        }
    }

    fun reset() {
        _amount.value = null
        _date.value = getCurrentDate()
        _note.value = null
        _category.value = null
        _wallet.value = null
        _imageBytes.value = null
        _recurrenceRate.value = null
        _validationState.value = ValidationState(shouldShowErrors = false)
        _uiState.value = UiState.Initial
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }
}
