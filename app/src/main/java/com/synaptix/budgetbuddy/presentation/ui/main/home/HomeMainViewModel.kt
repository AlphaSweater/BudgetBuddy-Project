package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetCategoriesUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeMainViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getTransactionUseCase: GetTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "HomeMainViewModel"
    }

    private val _wallets = MutableLiveData<List<Wallet>>()
    val wallets: LiveData<List<Wallet>> = _wallets

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private var _selectedStartDate: String = ""
    var selectedStartDate: String
        get() = _selectedStartDate
        set(value) {
            _selectedStartDate = value
        }
    private var _selectedEndDate: String = ""
    var selectedEndDate: String
        get() = _selectedEndDate
        set(value) {
            _selectedEndDate = value
        }


    private val _selectedTransaction = MutableLiveData<Transaction>()
    val selectedTransaction: LiveData<Transaction> = _selectedTransaction

    fun loadWallets() {
        Log.d(TAG, "loadWallets() called")
        viewModelScope.launch {
            try {
                val userId = getUserIdUseCase.execute()
                Log.d(TAG, "Retrieved userId: $userId")

                val walletList = getWalletUseCase.execute(userId)
                Log.d(TAG, "Fetched ${walletList.size} wallets for user")

                _wallets.value = walletList
            } catch (e: Exception) {
                Log.e(TAG, "Error loading wallets: ${e.message}", e)
            }
        }
    }

    fun loadTransactions() {
        Log.d(TAG, "loadTransactions() called")
        viewModelScope.launch {
            try {
                val userId = getUserIdUseCase.execute()
                Log.d(TAG, "Retrieved userId: $userId")

                val transactionList = getTransactionUseCase.execute(userId)
                Log.d(TAG, "Fetched ${transactionList.size} transactions for user")

                _transactions.value = transactionList
            } catch (e: Exception) {
                Log.e(TAG, "Error loading wallets: ${e.message}", e)
            }
        }
    }

    fun loadCategories() {
        Log.d(TAG, "loadCategories() called")
        viewModelScope.launch {
            try {
                val userId = getUserIdUseCase.execute()
                Log.d(TAG, "Retrieved userId: $userId")

                val categoryList = getCategoriesUseCase.invoke(userId)
                Log.d(TAG, "Fetched ${categoryList.size} categories for user")

                _categories.value = categoryList.map { it }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories: ${e.message}", e)
            }
        }
    }

    fun setTransaction(transaction: Transaction) {
        _selectedTransaction.value = transaction
    }
}
