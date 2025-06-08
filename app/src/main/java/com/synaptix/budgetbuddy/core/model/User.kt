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
// Data Model: User
// Represents the full user entity.
//======================================================================================
data class User(
    override val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val lastLoginAt: Long = System.currentTimeMillis()
) : Entity {
    companion object {
        fun new(
            email: String,
            firstName: String? = null,
            lastName: String? = null,
            lastLoginAt: Long = System.currentTimeMillis()
        ): User = User(
            id = "",
            email = email,
            firstName = firstName,
            lastName = lastName,
            lastLoginAt = lastLoginAt
        )
    }
}
