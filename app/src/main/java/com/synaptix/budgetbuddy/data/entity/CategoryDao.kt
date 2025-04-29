package com.synaptix.budgetbuddy.data.entity

import com.synaptix.budgetbuddy.data.local.CategoryEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    //sql query to grab a category based on category ID
    @Query("SELECT * FROM category_table WHERE category_id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): CategoryEntity?
}