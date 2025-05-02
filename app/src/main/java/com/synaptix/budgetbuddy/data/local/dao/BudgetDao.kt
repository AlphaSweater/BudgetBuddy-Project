package com.synaptix.budgetbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.synaptix.budgetbuddy.data.entity.BudgetEntity


@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    //sql query to grab a budget based on budget ID
    @Query("SELECT * FROM budget_table WHERE budget_id = :budgetid")
    suspend fun getBudgetById(budgetid: Int): BudgetEntity?

    //sql query to grab all budgets for a user
    @Query("SELECT * FROM budget_table WHERE user_id = :userId")
    suspend fun getBudgetsByUserId(userId: Int): List<BudgetEntity>

    //sql query to grab all budgets for a wallet
    @Query("SELECT * FROM budget_table WHERE wallet_id = :walletId")
    suspend fun getBudgetsByWalletId(walletId: Int): List<BudgetEntity>
}