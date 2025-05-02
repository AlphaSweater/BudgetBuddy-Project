package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

data class Wallet (
    val walletId: Int = 0,
    val userId: Int,
    val walletName: String,
    val walletCurrency: String,
    var walletBalance: Double
) : Serializable