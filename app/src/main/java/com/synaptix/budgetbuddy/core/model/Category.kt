package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

data class Category (
    val categoryId: Int? = null,
    val userId: Int?,
    val categoryName: String,
    val categoryType: String,
    val categoryIcon: Int,
    val categoryColor: Int
) : Serializable