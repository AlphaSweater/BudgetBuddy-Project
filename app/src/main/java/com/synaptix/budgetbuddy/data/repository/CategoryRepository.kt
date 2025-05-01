package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.data.local.dao.CategoryDao
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
){
    suspend fun getCategoriesByUserId(userId: Int) = categoryDao.getCategoriesByUserId(userId)
}