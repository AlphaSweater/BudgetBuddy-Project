package com.synaptix.budgetbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.synaptix.budgetbuddy.data.entity.CategoryEntity

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity): Long

    //sql query to grab all categories based on user ID and a user ID 0
    @Query("SELECT * FROM category_table WHERE user_id = :userId OR user_id IS null")
    suspend fun getCategoriesByUserId(userId: Int): List<CategoryEntity>

}