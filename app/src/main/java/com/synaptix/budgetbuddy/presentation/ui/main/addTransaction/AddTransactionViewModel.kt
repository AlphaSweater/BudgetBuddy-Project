package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import java.util.UUID
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddTransactionUseCase
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.labelSelector.Label

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
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

    suspend fun addTransaction() {
        val transaction = Transaction(
            userId = getUserIdUseCase.execute(),
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
