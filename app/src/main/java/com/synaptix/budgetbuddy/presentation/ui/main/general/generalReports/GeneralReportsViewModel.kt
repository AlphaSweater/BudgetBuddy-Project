package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralReportsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val reportBudgetCategoryItem = MutableLiveData<List<BudgetListItems.BudgetCategoryItem>>()

    val transactions: LiveData<List<Transaction>> = _transactions

    fun loadTransactions() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            val result = getTransactionsUseCase.execute(userId)

            when (result) {
                is GetTransactionsUseCase.GetTransactionsResult.Success -> {
                    val transactionList = result.transactions
                    _transactions.postValue(transactionList)
                }
                is GetTransactionsUseCase.GetTransactionsResult.Error -> {
                    // Handle error here, e.g., show message or log
                    // You could clear the list or keep previous value
                    _transactions.postValue(emptyList()) // or keep old list
                }
            }
        }

//
//
//            val categoryGroups = result.groupBy { it.category?.categoryName ?: "Uncategorized" }
//
//            val items = categoryGroups.map { (name, transactions) ->
//                val icon = transactions.firstOrNull()?.category?.categoryIcon ?: R.drawable.ic_car_24
//                val colour = transactions.firstOrNull()?.category?.categoryColor ?: R.color.cat_orange
//                val amount = "R ${transactions.sumOf { it.amount }.toInt()}"
//                val relativeDate = "This Month" // You can implement actual logic here
//
//                BudgetReportListItems.CategoryItems(
//                    categoryName = name,
//                    categoryIcon = icon,
//                    categoryColour = colour,
//                    transactionCount = transactions.size,
//                    amount = amount,
//                    relativeDate = relativeDate
//                )
//            }

//            reportCategoryItems.postValue(items)
        }
    }