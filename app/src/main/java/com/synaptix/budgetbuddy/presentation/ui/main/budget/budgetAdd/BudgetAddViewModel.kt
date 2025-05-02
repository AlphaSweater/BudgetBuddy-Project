package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.synaptix.budgetbuddy.core.model.BudgetIn
import com.synaptix.budgetbuddy.core.usecase.main.budget.AddBudgetUseCase
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.model.WalletIn
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetAddViewModel @Inject constructor(
    private val addBudgetUseCase: AddBudgetUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    val budgetName = MutableLiveData<String?>()
    val wallet = MutableLiveData<Wallet?>()
    val category = MutableLiveData<Category?>()
    val budgetAmount = MutableLiveData<Double?>()


    suspend fun addBudget() {
        val budget = BudgetIn(
            userId = getUserIdUseCase.execute(),
            budgetName = budgetName.value ?: "",
            walletId = wallet.value?.walletId ?: 0,
            categoryId = category.value?.categoryId ?: 0,
            amount = budgetAmount.value ?: 0.0,
            spent = 00.0
        )
        addBudgetUseCase.execute(budget)
    }

    fun reset() {
        budgetName.value = null
        wallet.value = null
        category.value = null
        budgetAmount.value = null
    }
}