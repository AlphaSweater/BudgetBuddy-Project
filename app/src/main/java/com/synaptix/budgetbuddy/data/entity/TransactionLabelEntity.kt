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
// TransactionLabel Entity
// ================================
// This is a junction table to represent the many-to-many relationship 
// between Transactions and Labels.
//
@Entity(
    tableName = "transaction_label_table",
    primaryKeys = ["transaction_id", "label_id"] // Composite primary key
)
data class TransactionLabelEntity (
    val transaction_id: Int = 0, // ID of the linked transaction
    val label_id: Int = 0        // ID of the linked label
)
