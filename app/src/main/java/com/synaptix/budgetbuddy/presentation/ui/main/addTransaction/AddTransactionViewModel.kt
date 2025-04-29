package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddTransactionViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is an Add Transaction Fragment"
    }
    val text: LiveData<String> = _text
}