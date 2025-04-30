package com.synaptix.budgetbuddy.core.model

data class Transaction(
    val userId: Int,
    val transactionId: String,
    val walletId: String,
    val category: String,
    val currencyType: String,
    val amount: Double,
    val date: String,
    val note: String?,
    val selectedLabels: List<Any?> = mutableListOf(),
    val photo: String?,
    val recurrenceRate: String?
)