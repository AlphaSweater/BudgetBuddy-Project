package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel
import com.synaptix.budgetbuddy.core.model.Transaction
import java.util.UUID
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddTransactionUseCase
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.labelSelector.Label

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    val category = MutableLiveData<String>()
    val walletId = MutableLiveData<String>()
    val currency = MutableLiveData<String>()
    val amount = MutableLiveData<Double>()
    val date = MutableLiveData<String>()
    val note = MutableLiveData<String?>()
    val labels = MutableLiveData<List<Label>>(emptyList())
    val photo = MutableLiveData<String?>()
    val recurrenceRate = MutableLiveData<String?>()

    fun addTransaction() {
        val transaction = Transaction(
            userId = "user123",
            transactionId = UUID.randomUUID().toString(),
            category = category.value ?: "",
            walletId = walletId.value ?: "",
            currencyType = currency.value ?: "",
            amount = amount.value ?: 0.0,
            date = date.value ?: "",
            note = note.value,
            selectedLabels = labels.value ?: emptyList(),
            photo = photo.value,
            recurrenceRate = recurrenceRate.value
        )
        addTransactionUseCase.execute(transaction)
    }
}
