// ======================================================================================
// Group 2 - Group Members:
// ======================================================================================
// * Chad Fairlie ST10269509
// * Dhiren Ruthenavelu ST10256859
// * Kayla Ferreira ST10259527
// * Nathan Teixeira ST10249266
// ======================================================================================
// Declaration:
// ======================================================================================
// We declare that this work is our own original work and that no part of it has been
// copied from any other source, except where explicitly acknowledged.
// ======================================================================================
// References:
// ======================================================================================
// * ChatGPT was used to help with the design and planning. As well as assisted with
//   finding and fixing errors in the code.
// * ChatGPT also helped with the forming of comments for the code.
// * https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
// ======================================================================================

package com.synaptix.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

//
// ================================
// User Entity
// ================================
// Represents the user account data stored in the database.
//
@Entity(
    tableName = "user_table"
)
data class UserEntity (
    @PrimaryKey(autoGenerate = true) val user_id: Int, // Unique user ID
    val firstName: String?, // Optional first name
    val lastName: String?,  // Optional last name
    val email: String,      // User's email (unique)
    val password: String    // User's password
)
