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
// Wallet Entity
// ================================
// Represents a wallet associated with a user, holding balance and currency info.
//
@Entity(
    tableName = "wallet_table",
    // AI assisted with the creation of this foreign key
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"]
        )
    ]
)
data class WalletEntity (
    @PrimaryKey(autoGenerate = true) val wallet_id: Int, // Unique wallet ID
    val user_id: Int,           // ID of the user who owns this wallet
    val name: String,           // Name of the wallet (e.g., "Savings", "Cash")
    val currency: String,       // Currency type (e.g., "USD", "ZAR")
    val balance: Double,        // Current balance of the wallet
    val excludeFromTotal: Boolean = false // Flag to exclude this wallet from total calculations
)
