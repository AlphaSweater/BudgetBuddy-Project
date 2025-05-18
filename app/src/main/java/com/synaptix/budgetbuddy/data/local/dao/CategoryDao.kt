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
import com.synaptix.budgetbuddy.core.model.CategoryColor
import com.synaptix.budgetbuddy.core.model.CategoryIcon
import com.synaptix.budgetbuddy.data.entity.CategoryColorEntity
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.CategoryIconEntity
import com.synaptix.budgetbuddy.data.entity.relations.CategoryWithUser

@Dao
interface CategoryDao {

    // Inserts a new category into the database. If the category already exists, it ignores it.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity): Long

    //sql query to grab all categories based on user ID and a user ID 0
    @Query("SELECT * FROM category_table WHERE user_id = :userId OR user_id IS null")
    suspend fun getCategoriesByUserId(userId: Int): List<CategoryWithUser>

}

@Dao
interface CategoryIconDao {
    @Query("SELECT * FROM category_icons_table")
    suspend fun getAllIcons(): List<CategoryIconEntity>

    @Insert
    suspend fun insertAll(icons: List<CategoryIconEntity>)

    @Query("DELETE FROM category_icons_table")
    suspend fun deleteAll()
}

@Dao
interface CategoryColorDao {
    @Query("SELECT * FROM category_colors_table")
    suspend fun getAllColors(): List<CategoryColorEntity>

    @Insert
    suspend fun insertAll(colors: List<CategoryColorEntity>)

    @Query("DELETE FROM category_colors_table")
    suspend fun deleteAll()
}