//======================================================================================
// Group 2 - Group Members:
//======================================================================================
// * Chad Fairlie ST10269509
// * Dhiren Ruthenavelu ST10256859
// * Kayla Ferreira ST10259527
// * Nathan Teixeira ST10249266
//======================================================================================
// Declaration:
//======================================================================================
// We declare that this work is our own original work and that no part of it has been
// copied from any other source, except where explicitly acknowledged.
//======================================================================================
// References:
//======================================================================================
// * ChatGPT was used to help with the design and planning. As well as assisted with
//   finding and fixing errors in the code.
// * ChatGPT also helped with the forming of comments for the code.
// * https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.core.model

//======================================================================================
// Data Model: Transaction
// Represents the full transaction entity including related user, wallet, and category + label objects.
//======================================================================================
data class Transaction(
    override val id: String,
    val user: User,
    val wallet: Wallet,
    val category: Category,
    val labels: List<Label> = emptyList(),
    val amount: Double,
    val currency: String = "ZAR",
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val photoUrl: String? = null,
    val recurrenceRate: String? = null,
) : Entity {
    companion object {
        fun new(
            user: User,
            wallet: Wallet,
            category: Category,
            labels: List<Label> = emptyList(),
            amount: Double,
            currency: String = "ZAR",
            date: Long = System.currentTimeMillis(),
            note: String = "",
            photoUrl: String? = null,
            recurrenceRate: String? = null,
        ): Transaction = Transaction(
            id = "",
            user = user,
            wallet = wallet,
            category = category,
            labels = labels,
            amount = amount,
            currency = currency,
            date = date,
            note = note,
            photoUrl = photoUrl,
            recurrenceRate = recurrenceRate
        )
    }
}