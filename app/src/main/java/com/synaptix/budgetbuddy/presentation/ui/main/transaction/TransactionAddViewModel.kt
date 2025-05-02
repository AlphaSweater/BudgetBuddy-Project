package com.synaptix.budgetbuddy.presentation.ui.main.transaction

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.TransactionIn
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddTransactionUseCase

@HiltViewModel
class TransactionAddViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    val category = MutableLiveData<Category>()
    val wallet = MutableLiveData<Wallet>()
    val currency = MutableLiveData<String>()
    val amount = MutableLiveData<Double>()
    val date = MutableLiveData<String>()
    val note = MutableLiveData<String?>()
    var selectedLabels = MutableLiveData<List<Label>>(emptyList())
    private val _imageBytes = MutableLiveData<ByteArray?>()
    val imageBytes: LiveData<ByteArray?> = _imageBytes
    val recurrenceRate = MutableLiveData<String?>()


    suspend fun addTransaction() {
        val transaction = TransactionIn(
            userId = getUserIdUseCase.execute(),
            categoryId = category.value?.categoryId ?: 0,
            walletId = wallet.value?.walletId ?: 0,
            currencyType = currency.value ?: "",
            amount = amount.value ?: 0.0,
            date = date.value ?: "",
            note = note.value,
            selectedLabels = selectedLabels.value ?: emptyList<Label>(),
            photo = imageBytes.value,
            recurrenceRate = recurrenceRate.value
        )
        addTransactionUseCase.execute(transaction)
    }

    fun setImageBytes(bytes: ByteArray?) {
        _imageBytes.value = bytes
    }
}
