package com.synaptix.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entity representing a budget in the system.
 * Budgets are now linked to users and can have multiple categories through the BudgetCategoryCrossRef.
 */
@Entity(
    tableName = "budget_table",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val budget_id: Int,
    val user_id: Int,
    val name: String,
    val amount: Double,
    val spent: Double
)
