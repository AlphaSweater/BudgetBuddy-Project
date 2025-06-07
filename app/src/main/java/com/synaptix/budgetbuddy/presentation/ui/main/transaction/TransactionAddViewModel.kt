package com.synaptix.budgetbuddy.presentation.ui.main.transaction

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.RecurrenceData
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
import java.io.Serializable

@HiltViewModel
class TransactionAddViewModel @Inject constructor(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    enum class ScreenMode : Serializable {
        VIEW, EDIT, CREATE
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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // UI State
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Screen Mode
    private val _screenMode = MutableStateFlow<ScreenMode>(ScreenMode.CREATE)
    val screenMode: StateFlow<ScreenMode> = _screenMode

    fun setScreenMode(mode: ScreenMode, transaction: Transaction? = null) {
        _screenMode.value = mode
        _transaction.value = transaction

        when (mode) {
            ScreenMode.VIEW, ScreenMode.EDIT -> {
                transaction?.let { populateTransactionData(it) }
            }
            ScreenMode.CREATE -> {
                reset()
            }
        }
    }

    init {
        val mode = savedStateHandle.get<String>("screenMode") ?: "CREATE"
        _screenMode.value = ScreenMode.valueOf(mode)
    }

    private val _transaction = MutableStateFlow<Transaction?>(null)
    val transaction: StateFlow<Transaction?> = _transaction

    fun setTransaction(transaction: Transaction?) {
        _transaction.value = transaction
        transaction?.let { populateTransactionData(it) }
    }

    private fun populateTransactionData(transaction: Transaction) {
        _amount.value = transaction.amount
        _currency.value = transaction.currency
        _note.value = transaction.note
        _date.value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(transaction.date))
        _category.value = transaction.category
        _wallet.value = transaction.wallet
        _selectedLabels.value = transaction.labels
        _recurrenceData.value = transaction.recurrenceData
        // Note: Image handling will be done separately
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Validation State
    private val _validationState = MutableStateFlow(ValidationState())
    val validationState: StateFlow<ValidationState> = _validationState

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Form Fields
    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category

    fun setCategory(category: Category?) {
        _category.value = category
        validateForm()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _wallet = MutableStateFlow<Wallet?>(null)
    val wallet: StateFlow<Wallet?> = _wallet

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _currency = MutableStateFlow("ZAR")
    val currency: StateFlow<String> = _currency

    fun setCurrency(currency: String) {
        _currency.value = currency
        validateForm()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _amount = MutableStateFlow<Double?>(null)
    val amount: StateFlow<Double?> = _amount

    fun setAmount(amount: Double?) {
        _amount.value = amount
        validateForm()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _date = MutableStateFlow(getCurrentDate())
    val date: StateFlow<String> = _date

    fun setDate(date: String) {
        _date.value = date
        validateForm()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _note = MutableStateFlow<String?>(null)
    val note: StateFlow<String?> = _note

    fun setNote(note: String) {
        _note.value = note
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _imageBytes = MutableStateFlow<ByteArray?>(null)
    val imageBytes: StateFlow<ByteArray?> = _imageBytes

    fun setImageBytes(bytes: ByteArray?) {
        _imageBytes.value = bytes
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _recurrenceData = MutableStateFlow(RecurrenceData.DEFAULT)
    val recurrenceData: StateFlow<RecurrenceData> = _recurrenceData

    fun setRecurrenceData(data: RecurrenceData) {
        _recurrenceData.value = data
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _selectedLabels = MutableStateFlow<List<Label>>(emptyList())
    val selectedLabels: StateFlow<List<Label>> = _selectedLabels

    fun setLabels(labels: List<Label>) {
        _selectedLabels.value = labels
    }

    fun removeLabel(label: Label) {
        val currentLabels = _selectedLabels.value.toMutableList()
        currentLabels.remove(label)
        _selectedLabels.value = currentLabels
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _saveState = MutableStateFlow(false)
    val saveState: StateFlow<Boolean> = _saveState

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Function to validate the form inputs
    fun validateForm(): Boolean {
        val currentState = _validationState.value
        val isAmountValid = (_amount.value ?: 0.0) > 0.0
        val isCurrencyValid = !_currency.value.isNullOrBlank()
        val isCategoryValid = _category.value != null
        val isWalletValid = _wallet.value != null
        val isDateValid = !_date.value.isNullOrBlank()

        _saveState.value = true
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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun showValidationErrors() {
        _validationState.value = _validationState.value.copy(shouldShowErrors = true)
        validateForm()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Function to add a new transaction
    fun addTransaction() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                val userId = getUserIdUseCase.execute()
                if (userId.isEmpty()) {
                    _uiState.value = UiState.Error("User ID is empty")
                    return@launch
                }

                // Create transaction object first without image
                val tempUser = User(userId, "", "", "")
                val newTransaction = Transaction.new(
                    user = tempUser,
                    wallet = _wallet.value!!,
                    category = _category.value!!,
                    labels = _selectedLabels.value,
                    amount = _amount.value ?: 0.0,
                    currency = _currency.value,
                    date = parseDate(_date.value),
                    note = _note.value ?: "",
                    photoUrl = null, // Will be updated after upload
                    recurrenceData = _recurrenceData.value
                )

                // Upload image in parallel if exists
                val imageUrl = _imageBytes.value?.let { bytes ->
                    try {
                        when (val result = uploadImageUseCase.execute(bytes)) {
                            is UploadImageUseCase.UploadImageResult.Success -> result.imageUrl
                            is UploadImageUseCase.UploadImageResult.Error -> {
                                Log.e("TransactionAddViewModel", "Error uploading image: ${result.message}")
                                null
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("TransactionAddViewModel", "Exception uploading image: ${e.message}")
                        null
                    }
                }

                // Update transaction with image URL if upload was successful
                val finalTransaction = if (imageUrl != null) {
                    newTransaction.copy(photoUrl = imageUrl)
                } else {
                    newTransaction
                }

                // Save transaction
                addTransactionUseCase.execute(finalTransaction)
                    .catch { e ->
                        Log.e("TransactionAddViewModel", "Error in transaction flow: ${e.message}")
                        _uiState.value = UiState.Error(e.message ?: "Failed to add transaction")
                    }
                    .collect { result ->
                        when (result) {
                            is AddTransactionResult.Success -> {
                                Log.d("TransactionAddViewModel", "Transaction added successfully: ${result.transactionId}")
                                reset()
                                _uiState.value = UiState.Success
                            }
                            is AddTransactionResult.Error -> {
                                Log.e("TransactionAddViewModel", "Error adding transaction: ${result.message}")
                                _uiState.value = UiState.Error(result.message)
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("TransactionAddViewModel", "Exception adding transaction: ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Failed to add transaction")
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun reset() {
        _saveState.value = false
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
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun getCurrentDate(): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun parseDate(dateStr: String): Long = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .parse(dateStr)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\