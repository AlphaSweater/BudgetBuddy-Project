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

package com.synaptix.budgetbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.synaptix.budgetbuddy.data.entity.BudgetEntity
import com.synaptix.budgetbuddy.data.entity.relations.BudgetWithDetail


@Dao
interface BudgetDao {

    // Inserts a new budget into the database. If the budget already exists, it ignores it.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    //sql query to grab a budget based on budget ID
    @Query("SELECT * FROM budget_table WHERE budget_id = :budgetid")
    suspend fun getBudgetById(budgetid: Int): BudgetEntity?

    //sql query to grab all budgets for a user
    @Query("SELECT * FROM budget_table WHERE user_id = :userId")
    suspend fun getBudgetsByUserId(userId: Int): List<BudgetWithDetail>

    //sql query to grab all budgets for a wallet
    @Query("SELECT * FROM budget_table WHERE wallet_id = :walletId")
    suspend fun getBudgetsByWalletId(walletId: Int): List<BudgetEntity>

    @Query("UPDATE budget_table SET amount = :amount WHERE wallet_id = :budgetId")
    suspend fun updateAmount(budgetId: Long, amount: Double)
}