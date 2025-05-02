package com.synaptix.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

//
// ================================
// Budget Entity
// ================================
// Represents a budget assigned to a user and linked to a wallet.
@Entity(
    tableName = "budget_table",
    foreignKeys = [
        // Links budget to UserEntity
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"]
        ),
        // Links budget to WalletEntity
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["wallet_id"],
            childColumns = ["wallet_id"]
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"]
        )
    ]
)
data class BudgetEntity (
    @PrimaryKey(autoGenerate = true) val budget_id: Int,
    val user_id: Int,
    val wallet_id: Int,
    val category_id: Int,
    val name: String,
    val amount: Double,
    val spent: Double
)
