package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.data.local.dao.CategoryDao
import com.synaptix.budgetbuddy.data.local.dao.UserDao
import com.synaptix.budgetbuddy.data.local.mapper.toDomain
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val userDao: UserDao
){
    suspend fun getCategoriesByUserId(userId: Int): List<Category> {
        val categories = categoryDao.getCategoriesByUserId(userId)
        return categories.map { it.toDomain() }
    }
}