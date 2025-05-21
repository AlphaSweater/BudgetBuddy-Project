package com.synaptix.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Cross-reference entity for the many-to-many relationship between Budget and Category.
 * This allows budgets to have multiple categories and categories to be assigned to multiple budgets.
 */
@Entity(
    tableName = "budget_category_cross_ref",
    foreignKeys = [
        ForeignKey(
            entity = BudgetEntity::class,
            parentColumns = ["budget_id"],
            childColumns = ["budget_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BudgetCategoryCrossRef(
    @PrimaryKey(autoGenerate = true) val budget_category_id: Int,
    val budget_id: Int,
    val category_id: Int
) 