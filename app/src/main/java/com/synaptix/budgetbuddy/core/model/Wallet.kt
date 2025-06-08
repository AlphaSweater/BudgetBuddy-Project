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

//======================================================================================
// Data Model: Wallet
// Represents the full wallet entity including related user object.
//======================================================================================
data class Wallet(
    override val id: String,
    val user: User,
    val name: String,
    val currency: String = "ZAR",
    val balance: Double = 0.0,
    val minGoal: Double = 0.0,
    val maxGoal: Double = 0.0,
    val excludeFromTotal: Boolean = false,
    val lastTransactionAt: Long? = null
) : Entity {
    companion object {
        fun new(
            user: User,
            name: String,
            currency: String = "ZAR",
            balance: Double = 0.0,
            minGoal: Double = 0.0,
            maxGoal: Double = 0.0,
            excludeFromTotal: Boolean = false,
            lastTransactionAt: Long? = null
        ): Wallet = Wallet(
            id = "",
            user = user,
            name = name,
            currency = currency,
            balance = balance,
            minGoal = minGoal,
            maxGoal = maxGoal,
            excludeFromTotal = excludeFromTotal,
            lastTransactionAt = lastTransactionAt
        )
    }
}
