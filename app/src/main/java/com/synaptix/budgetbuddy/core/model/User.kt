package com.synaptix.budgetbuddy.core.model

data class User (
    val userId: Int,
    val name: String?,
    val surname: String?,
    val email: String,
    val password: String
)