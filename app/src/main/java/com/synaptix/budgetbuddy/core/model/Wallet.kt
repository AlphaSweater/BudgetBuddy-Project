//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

// Data class representing a WalletIn (for input operations)
data class WalletIn (
    // Unique identifier for the wallet (default is 0)
    val walletId: Int = 0,

    // ID of the user to whom the wallet belongs
    val userId: Int,

    // Name of the wallet
    val walletName: String,

    // Currency type of the wallet (e.g., USD, EUR)
    val walletCurrency: String,

    // Current balance in the wallet
    var walletBalance: Double,

    // Flag indicating if the wallet should be excluded from total calculations
    val excludeFromTotal: Boolean = false
) : Serializable

// Data class representing a Wallet (with associated User)
data class Wallet (
    // Unique identifier for the wallet (default is 0)
    val walletId: Int = 0,

    // User to whom the wallet belongs (optional, can be null)
    val user: User?,

    // Name of the wallet
    val walletName: String,

    // Currency type of the wallet (e.g., USD, EUR)
    val walletCurrency: String,

    // Current balance in the wallet
    var walletBalance: Double,

    // Flag indicating if the wallet should be excluded from total calculations
    val excludeFromTotal: Boolean = false
)
