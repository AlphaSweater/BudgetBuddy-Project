package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

data class Budget (
    val budgetId: Int? = null,
    val userId: Int,
    val walletId: Int,
    val budgetName: String,
    val goalMinAmount: Double,
    val goalMaxAmount: Double
) : Serializable