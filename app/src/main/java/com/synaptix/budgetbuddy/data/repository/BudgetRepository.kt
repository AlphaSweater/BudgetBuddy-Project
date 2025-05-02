package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.entity.BudgetEntity
import com.synaptix.budgetbuddy.data.entity.mapper.toDomain
import com.synaptix.budgetbuddy.data.local.dao.BudgetDao
import com.synaptix.budgetbuddy.data.local.dao.WalletDao
import javax.inject.Inject

class BudgetRepository @Inject constructor(private val budgetDao: BudgetDao) {
    suspend fun getBudgetsByUserId(userId: Int): List<Budget> {
        val budgets = budgetDao.getBudgetsByUserId(userId)
        return budgets.map { it.toDomain() }
    }

    suspend fun insertBudget(budget: BudgetEntity) {
        budgetDao.insertBudget(budget)
    }
}