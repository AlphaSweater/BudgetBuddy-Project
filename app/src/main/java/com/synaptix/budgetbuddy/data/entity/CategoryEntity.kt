package com.synaptix.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "category_table",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"]
        ),
    ]
)
data class CategoryEntity (
    @PrimaryKey(autoGenerate = true) val category_id: Int,
    val user_id: Int,
    val name: String,
    val colour: String,
    val icon: String
)