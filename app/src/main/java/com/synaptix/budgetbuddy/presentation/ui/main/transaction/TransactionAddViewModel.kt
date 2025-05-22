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

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _validationState = MutableLiveData(ValidationState())
    val validationState: LiveData<ValidationState> = _validationState

    // Form fields
    private val _category = MutableLiveData<Category?>()
    val category: LiveData<Category?>
        get() = _category

    private val _wallet = MutableLiveData<Wallet?>()
    val wallet: LiveData<Wallet?>
        get() = _wallet

    private val _currency = MutableLiveData("ZAR")
    val currency: LiveData<String>
        get() = _currency

    private val _amount = MutableLiveData<Double?>()
    val amount: LiveData<Double?>
        get() = _amount

    private val _date = MutableLiveData(getCurrentDate())
    val date: LiveData<String>
        get() = _date

    private val _note = MutableLiveData<String?>()
    val note: LiveData<String?>
        get() = _note

    private val _imageBytes = MutableLiveData<ByteArray?>()
    val imageBytes: LiveData<ByteArray?>
        get() = _imageBytes

    private val _recurrenceData = MutableLiveData<RecurrenceData>()
    val recurrenceData: LiveData<RecurrenceData>
        get() = _recurrenceData

    var selectedLabels = MutableLiveData<List<Label>>(emptyList())

    var saveState = MutableLiveData<Boolean>(false)

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

    fun setRecurrenceData(data: RecurrenceData) {
        _recurrenceData.value = data
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

        saveState.value = true
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
                val userId = getUserIdUseCase.execute()
                val tempUser = User(userId, "", "", "")
                val newTransaction = Transaction.new(
                    user = tempUser,
                    wallet = _wallet.value!!,
                    category = _category.value!!,
                    labels = selectedLabels.value!!,
                    amount = _amount.value ?: 0.0,
                    currency = _currency.value ?: "ZAR",
                    date = parseDate((_date.value ?: System.currentTimeMillis()).toString()),
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
        _validationState.value = ValidationState(shouldShowErrors = false)
        _uiState.value = UiState.Initial
        saveState.value = false
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
