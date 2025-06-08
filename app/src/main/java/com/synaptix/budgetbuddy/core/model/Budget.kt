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

//======================================================================================
// Data Model: Budget
// Represents the full budget entity including related user object.
//======================================================================================
data class Budget(
    val id: String,
    val user: User,
    val name: String,
    val amount: Double,
    val categories: List<Category>,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val isRecurring: Boolean = false,
    val recurrencePeriod: String? = null
)