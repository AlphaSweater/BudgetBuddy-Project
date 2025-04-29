package com.synaptix.budgetbuddy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "category_table"
)
data class CategoryEntity (
    @PrimaryKey(autoGenerate = true) val category_id: Int,
    val name: String
)