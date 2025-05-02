package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

data class CategoryIn (
    val categoryId: Int = 0,
    val userId: Int?,
    val categoryName: String,
    val categoryType: String,
    val categoryIcon: Int,
    val categoryColor: Int
) : Serializable

data class Category(
    val categoryId: Int = 0,
    val user: User?,
    val categoryName: String,
    val categoryType: String,
    val categoryIcon: Int,
    val categoryColor: Int
)