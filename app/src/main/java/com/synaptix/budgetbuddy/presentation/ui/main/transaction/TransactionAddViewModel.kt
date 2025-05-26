package com.synaptix.budgetbuddy.presentation.ui.main.transaction

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.RecurrenceData
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddTransactionUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddTransactionUseCase.AddTransactionResult
import com.synaptix.budgetbuddy.core.usecase.main.transaction.UploadImageUseCase

@HiltViewModel
class TransactionAddViewModel @Inject constructor(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val uploadImageUseCase: UploadImageUseCase
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

    // UI State
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    // Validation State
    private val _validationState = MutableLiveData(ValidationState())
    val validationState: LiveData<ValidationState> = _validationState

    // Form Fields
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

    private val _recurrenceData = MutableLiveData<RecurrenceData>()
    val recurrenceData: LiveData<RecurrenceData> = _recurrenceData

    private val _selectedLabels = MutableLiveData<List<Label>>(emptyList())
    val selectedLabels: LiveData<List<Label>> = _selectedLabels

    val saveState = MutableLiveData<Boolean>(false)

    fun setCategory(category: Category?) {
        _category.postValue(category)
        validateForm()
    }

    fun setLabels(labels: List<Label>) {
        _selectedLabels.postValue(labels)
    }

    fun setWallet(wallet: Wallet?) {
        _wallet.postValue(wallet)
        validateForm()
    }

    fun setCurrency(currency: String) {
        _currency.postValue(currency)
        validateForm()
    }

    fun setAmount(amount: Double?) {
        _amount.postValue(amount)
        validateForm()
    }

    fun setDate(date: String) {
        _date.postValue(date)
        validateForm()
    }

    fun setNote(note: String) {
        _note.postValue(note)
    }

    fun setRecurrenceData(data: RecurrenceData) {
        _recurrenceData.postValue(data)
    }

    fun setImageBytes(bytes: ByteArray?) {
        _imageBytes.postValue(bytes)
    }

    fun removeLabel(label: Label) {
        val currentLabels = _selectedLabels.value?.toMutableList() ?: mutableListOf()
        currentLabels.remove(label)
        _selectedLabels.postValue(currentLabels)
    }

    fun validateForm(): Boolean {
        val currentState = _validationState.value ?: ValidationState()
        val isAmountValid = (_amount.value ?: 0.0) > 0.0
        val isCurrencyValid = !_currency.value.isNullOrBlank()
        val isCategoryValid = _category.value != null
        val isWalletValid = _wallet.value != null
        val isDateValid = !_date.value.isNullOrBlank()

        saveState.value = true
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
        _validationState.value = _validationState.value?.copy(shouldShowErrors = true)
        validateForm()
    }

    fun addTransaction() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val userId = getUserIdUseCase.execute()
                val newTransaction = Transaction.new(
                    user = User(userId, "", "", ""),
                    wallet = _wallet.value!!,
                    category = _category.value!!,
                    labels = _selectedLabels.value!!,
                    amount = _amount.value ?: 0.0,
                    currency = _currency.value ?: "ZAR",
                    date = parseDate(_date.value ?: System.currentTimeMillis().toString()),
                    note = _note.value ?: "",
                    photoUrl = null, // TODO: Upload image to Firebase Storage
                    recurrenceData = _recurrenceData.value ?: RecurrenceData.DEFAULT
                )

                when (val result = addTransactionUseCase.execute(newTransaction)) {
                    is AddTransactionResult.Success -> {
                        Log.d("TransactionAddViewModel", "Transaction added successfully: ${result.transactionId}")
                        _uiState.value = UiState.Success
                    }
                    is AddTransactionResult.Error -> {
                        Log.e("TransactionAddViewModel", "Error adding transaction: ${result.message}")
                        _uiState.value = UiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e("TransactionAddViewModel", "Exception adding transaction: ${e.message}")
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
        _recurrenceData.value = RecurrenceData.DEFAULT
        _selectedLabels.value = emptyList()
        _validationState.value = ValidationState(shouldShowErrors = false)
        _uiState.value = UiState.Initial
        saveState.value = false
    }

    private fun getCurrentDate(): String = 
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    private fun parseDate(dateStr: String): Long = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .parse(dateStr)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
