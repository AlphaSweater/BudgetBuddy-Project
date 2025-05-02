package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.synaptix.budgetbuddy.core.model.BudgetIn
import com.synaptix.budgetbuddy.core.usecase.main.budget.AddBudgetUseCase
import androidx.lifecycle.viewModelScope
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

    val budgetName = MutableLiveData("")
    val budgetAmount = MutableLiveData(0.0)
    val walletId = MutableLiveData(0)

    fun addBudget() {
        viewModelScope.launch {
            val budget = BudgetIn(
                userId = getUserIdUseCase.execute(),
                walletId = walletId.value ?: 0,
                budgetName = budgetName.value ?: "",
                amount = budgetAmount.value ?: 0.0
            )
            addBudgetUseCase.execute(budget)
        }
    }
}