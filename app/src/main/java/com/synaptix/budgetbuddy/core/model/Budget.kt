package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

data class BudgetIn (
    val budgetId: Int = 0,
    val userId: Int,
    val walletId: Int,
    val budgetName: String,
    val amount: Double
) : Serializable

data class Budget (
    val budgetId: Int = 0,
    val user: User?,
    val walletId: Int,
    val budgetName: String,
    val amount: Double
)