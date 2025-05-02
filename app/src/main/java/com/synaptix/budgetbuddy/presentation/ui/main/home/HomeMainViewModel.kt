package com.synaptix.budgetbuddy.presentation.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.synaptix.budgetbuddy.core.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel

class HomeMainViewModel : ViewModel() {
    private val _selectedTransaction = MutableLiveData<Transaction>()
    val selectedTransaction: LiveData<Transaction> = _selectedTransaction

    fun setTransaction(transaction: Transaction) {
        _selectedTransaction.value = transaction
    }

}