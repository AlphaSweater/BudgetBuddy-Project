package com.synaptix.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "budget_table",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"]
        ),
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["wallet_id"],
            childColumns = ["wallet_id"]
        ),
    ]

)
data class BudgetEntity (
    @PrimaryKey(autoGenerate = true) val budget_id: Int,
    val user_id: Int,
    val wallet_id: Int,
    val name: String,
    val minAmount: Double,
    val maxAmount: Double
)