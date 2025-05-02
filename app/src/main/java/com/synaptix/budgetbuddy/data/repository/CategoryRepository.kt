package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.data.local.dao.CategoryDao
import com.synaptix.budgetbuddy.data.local.dao.UserDao
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val userDao: UserDao
){
//    suspend fun getCategoriesByUserId(userId: Int) = categoryDao.getCategoriesByUserId(userId)

    suspend fun getCategoriesByUserId(userId: Int): List<Category> {
        val categories = categoryDao.getCategoriesByUserId(userId)

        return categories.map { category ->
            val user = userDao.getUserById(category.user_id ?: 0)
            Category(category, user)
        }
    }
}