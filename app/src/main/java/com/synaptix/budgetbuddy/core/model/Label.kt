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
// Data Model: Label
// Represents the full Label entity including related user object.
//======================================================================================
data class Label(
    override val id: String,
    val user: User?, // Nullable to allow for default labels
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Not Mapped
    var isSelected: Boolean = false
) : Entity {
    companion object {
        fun new(
            user: User?,
            name: String,
            createdAt: Long = System.currentTimeMillis(),
            updatedAt: Long = System.currentTimeMillis()
        ): Label = Label(
            id = "",
            user = user,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
