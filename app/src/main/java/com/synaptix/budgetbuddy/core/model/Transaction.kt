package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

data class Transaction(
    val transactionId: Int? = null,
    val userId: Int,
    val walletId: Int,
    val categoryId: Int,
    val currencyType: String,
    val amount: Double,
    val date: String,
    val note: String?,
    val selectedLabels: List<Label> = mutableListOf(),
    val photo: String?,
    val recurrenceRate: String?
) : Serializable