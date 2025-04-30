package com.synaptix.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "category_table",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"]
        )
    ]
)
data class CategoryEntity (
    @PrimaryKey(autoGenerate = true) val category_id: Int,
    val user_id: Int?, //nullable to allow for default categories to be globally acessible
    val name: String,
    val colour: String,
    val icon: String,
    val type: String
)