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
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.entity.BudgetEntity
import com.synaptix.budgetbuddy.data.entity.mapper.toDomain
import com.synaptix.budgetbuddy.data.local.dao.BudgetDao
import com.synaptix.budgetbuddy.data.local.dao.WalletDao
import javax.inject.Inject

// ===================================
// BudgetRepository
// ===================================
// This repository handles operations related to budgets,
// including retrieving and inserting budgets using the BudgetDao.
class BudgetRepository @Inject constructor(private val budgetDao: BudgetDao) {

    // ===================================
    // getBudgetsByUserId - Fetch Budgets for User
    // ===================================
    suspend fun getBudgetsByUserId(userId: Int): List<Budget> {
        val budgets = budgetDao.getBudgetsByUserId(userId)
        return budgets.map { it.toDomain() }
    }

    // ===================================
    // insertBudget - Insert New Budget
    // ===================================
    suspend fun insertBudget(budget: BudgetEntity) {
        budgetDao.insertBudget(budget)
    }
}
