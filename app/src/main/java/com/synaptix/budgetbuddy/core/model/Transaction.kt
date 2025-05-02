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

import androidx.room.Embedded
import androidx.room.Relation
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import java.io.Serializable

//======================================================================================
// Data Model: TransactionIn
// Represents the input data when creating or updating a transaction.
// Implements Serializable for easy passing between Android components.
//======================================================================================
data class TransactionIn(
    val transactionId: Int? = null,
    val userId: Int,
    val walletId: Int,
    val categoryId: Int,
    val currencyType: String,
    val amount: Double,
    val date: String,
    val note: String?,
    // val selectedLabels: List<Label> = mutableListOf(), // WIP: Work in progress feature
    val photo: ByteArray?,
    val recurrenceRate: String?
) : Serializable

//======================================================================================
// Data Model: Transaction
// Represents the full transaction entity including related user, wallet, and category.
//======================================================================================
data class Transaction(
    val transactionId: Int,
    val user: User?,         // Associated user (nullable)
    val wallet: Wallet?,     // Associated wallet (nullable)
    val category: Category?, // Associated category (nullable)
    val currencyType: String,
    val amount: Double,
    val date: String,
    val note: String?,
    val photo: ByteArray?,
    val recurrenceRate: String?
)
