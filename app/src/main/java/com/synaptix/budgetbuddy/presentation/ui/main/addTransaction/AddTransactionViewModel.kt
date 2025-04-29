package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel
import com.synaptix.budgetbuddy.core.model.Transaction
import java.util.UUID
import com.synaptix.budgetbuddy.core.usecase.AddTransactionUseCase

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    fun addTransaction(
        category: String,
        walletId: String,
        currencyType: String,
        amount: Double,
        date: String,
        note: String?,
        labels: List<String>,
        photo: String?,
        recurrenceRate: String?
    ) {
        val transaction = Transaction(
            userId = "user123", // Replace with actual user ID
            transactionId = UUID.randomUUID().toString(),
            category = category,
            walletId = walletId,
            currencyType = currencyType,
            amount = amount,
            date = date,
            note = note,
            labels = labels,
            photo = photo,
            recurrenceRate = recurrenceRate
        )

        addTransactionUseCase.execute(transaction)
    }
}
