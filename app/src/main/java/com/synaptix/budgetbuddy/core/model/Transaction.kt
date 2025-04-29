package com.synaptix.budgetbuddy.core.model

data class Transaction(
    val userId: Int,
    val transactionId: Int,
    val walletId: Int,
    val category: String,
    val currencyType: String,
    val amount: Double,
    val date: String,
    val note: String?,
    val labels: List<String>,
    val photo: String?,
    val recurrenceRate: String?
)