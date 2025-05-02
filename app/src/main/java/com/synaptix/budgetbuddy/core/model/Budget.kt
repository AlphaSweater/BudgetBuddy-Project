package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

data class BudgetIn (
    val budgetId: Int = 0,
    val userId: Int,
    val budgetName: String,
    val walletId: Int,
    val categoryId: Int,
    val amount: Double,
    val spent: Double
) : Serializable

data class Budget (
    val budgetId: Int = 0,
    val user: User?,
    val budgetName: String,
    val wallet: Wallet?,
    val category: Category?,
    val amount: Double,
    val spent: Double
)