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
import androidx.room.Transaction
import com.synaptix.budgetbuddy.data.entity.BudgetCategoryCrossRef
import com.synaptix.budgetbuddy.data.entity.BudgetEntity
import com.synaptix.budgetbuddy.data.entity.relations.BudgetWithDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetCategories(crossRefs: List<BudgetCategoryCrossRef>)

    @Transaction
    suspend fun insertBudgetWithCategories(
        budget: BudgetEntity,
        categoryIds: List<Int>
    ): Long {
        val budgetId = insertBudget(budget)
        val crossRefs = categoryIds.map { categoryId ->
            BudgetCategoryCrossRef(
                budget_id = budgetId.toInt(),
                category_id = categoryId
            )
        }
        insertBudgetCategories(crossRefs)
        return budgetId
    }

    @Query("SELECT * FROM budget_table WHERE user_id = :userId")
    fun getBudgetsByUser(userId: Int): List<BudgetWithDetail>

    @Transaction
    @Query("SELECT * FROM budget_table WHERE budget_id = :budgetId")
    fun getBudgetById(budgetId: Int): BudgetWithDetail?

    @Query("DELETE FROM budget_table WHERE budget_id = :budgetId")
    suspend fun deleteBudget(budgetId: Int)

    @Query("DELETE FROM budget_category_cross_ref WHERE budget_id = :budgetId")
    suspend fun deleteBudgetCategories(budgetId: Int)

    @Transaction
    suspend fun deleteBudgetWithCategories(budgetId: Int) {
        deleteBudgetCategories(budgetId)
        deleteBudget(budgetId)
    }
}