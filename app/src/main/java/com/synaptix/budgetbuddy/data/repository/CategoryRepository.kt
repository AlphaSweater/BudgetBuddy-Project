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

import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.local.dao.CategoryDao
import com.synaptix.budgetbuddy.data.local.dao.UserDao
import com.synaptix.budgetbuddy.data.entity.mapper.toDomain
import javax.inject.Inject

// ===================================
// CategoryRepository
// ===================================
// This repository handles operations related to categories,
// including retrieving categories linked to a specific user.
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val userDao: UserDao
) {
    // ===================================
    // getCategoriesByUserId - Fetch Categories for User
    // ===================================
    suspend fun getCategoriesByUserId(userId: Int): List<Category> {
        val categories = categoryDao.getCategoriesByUserId(userId)
        return categories.map { it.toDomain() }
    }

    suspend fun addCategory(entity: CategoryEntity): Long {
        return categoryDao.insertCategory(entity)
    }
}