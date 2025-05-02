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
//  finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

//======================================================================================
// Data Model: BudgetIn
// Represents the input data for creating or updating a budget.
// Implements Serializable for easy passing between Android components.
//======================================================================================
data class BudgetIn (
    val budgetId: Int = 0,
    val userId: Int,
    val walletId: Int,
    val budgetName: String,
    val amount: Double
) : Serializable

//======================================================================================
// Data Model: Budget
// Represents the full budget entity including related user object.
//======================================================================================
data class Budget (
    val budgetId: Int = 0,
    val user: User?, // Nullable User object associated with this budget
    val walletId: Int,
    val budgetName: String,
    val amount: Double
)