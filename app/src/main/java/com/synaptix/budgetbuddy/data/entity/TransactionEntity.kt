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
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

//
// ================================
// Transaction Entity
// ================================
// Represents a financial transaction made by the user.
// Each transaction is linked to a user, wallet, and category.
// Can optionally have a note, image, and recurrence pattern.
//
@Entity(
    tableName = "transaction_table",
    foreignKeys = [
        // Links transaction to the UserEntity
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"]
        ),
        // Links transaction to the WalletEntity
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["wallet_id"],
            childColumns = ["wallet_id"]
        ),
        // Links transaction to the CategoryEntity
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"]
        )
    ]
)
data class TransactionEntity (
    @PrimaryKey(autoGenerate = true) val transaction_id: Int,
    val user_id: Int,
    val wallet_id: Int,
    val category_id: Int,
    val amount: Double,
    val date: String,
    val note: String?,     // Optional note or description for the transaction
    val currency: String,  // Currency type (e.g., USD, ZAR)
    val image: ByteArray?, // Optional image attachment (e.g., receipt)
    val recurrence: String? // Optional recurrence pattern (e.g., monthly)
)
