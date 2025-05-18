package com.synaptix.budgetbuddy.core.model

data class CategoryColor(
    val id: Int = 0,
    val name: String,
    val colorValue: Int, // Color resource ID
    val hexCode: String
)