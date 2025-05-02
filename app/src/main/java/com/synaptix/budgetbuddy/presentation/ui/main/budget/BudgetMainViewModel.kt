package com.synaptix.budgetbuddy.presentation.ui.main.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.budget.GetBudgetUseCase
import com.synaptix.budgetbuddy.core.usecase.main.wallet.GetWalletUseCase
import com.synaptix.budgetbuddy.data.entity.MinMaxGoalEntity
import com.synaptix.budgetbuddy.data.local.dao.MinMaxGoalsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetMainViewModel @Inject constructor(
    private val getBudgetUseCase: GetBudgetUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val minMaxGoalsDao: MinMaxGoalsDao
) : ViewModel() {

    private val _budgets = MutableLiveData<List<Budget>>()
    val budgets: LiveData<List<Budget>> = _budgets

    private val _minMaxGoal = MutableLiveData<MinMaxGoalEntity?>()
    val minMaxGoal: LiveData<MinMaxGoalEntity?> = _minMaxGoal

    fun fetchBudgets() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            val budgetsList = getBudgetUseCase.execute(userId)
            _budgets.value = budgetsList
            _minMaxGoal.value = minMaxGoalsDao.getGoalsForUser(userId)

        }
    }
    fun saveMinMaxGoals(min: Double, max: Double) {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            minMaxGoalsDao.insertMinMaxGoal(MinMaxGoalEntity(minMaxGoalId = 0, user_id = userId, minGoal = min, maxGoal = max))
            _minMaxGoal.value = minMaxGoalsDao.getGoalsForUser(userId)
        }
    }
}