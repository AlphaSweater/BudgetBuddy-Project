package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

data class WalletIn (
    val walletId: Int = 0,
    val userId: Int,
    val walletName: String,
    val walletCurrency: String,
    var walletBalance: Double
) : Serializable

data class Wallet (
    val walletId: Int = 0,
    val user: User?,
    val walletName: String,
    val walletCurrency: String,
    var walletBalance: Double
)