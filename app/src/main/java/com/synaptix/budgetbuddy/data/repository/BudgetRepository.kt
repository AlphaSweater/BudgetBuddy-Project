//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.BudgetIn
import com.synaptix.budgetbuddy.data.entity.mapper.toDomain
import com.synaptix.budgetbuddy.data.entity.mapper.toEntity
import com.synaptix.budgetbuddy.data.local.dao.BudgetDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// ===================================
// BudgetRepository
// ===================================
// This repository handles operations related to budgets,
// including retrieving and inserting budgets using the BudgetDao.
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {

    // ===================================
    // getBudgetsByUserId - Fetch Budgets for User
    // ===================================
    fun getBudgetsByUserId(userId: Int): List<Budget> {
        return budgetDao.getBudgetsByUser(userId)
            .map { it.toDomain() }
    }

    fun getBudgetById(budgetId: Int): Budget? {
        val budget = budgetDao.getBudgetById(budgetId)
        return budget?.toDomain()
    }

    // ===================================
    // insertBudget - Insert New Budget
    // ===================================
    suspend fun insertBudget(budget: BudgetIn, categoryIds: List<Int>): Long {
        return budgetDao.insertBudgetWithCategories(
            budget = budget.toEntity(),
            categoryIds = categoryIds
        )
    }

    suspend fun deleteBudget(budgetId: Int) {
        budgetDao.deleteBudgetWithCategories(budgetId)
    }
}
