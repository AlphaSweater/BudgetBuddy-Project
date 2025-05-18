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
import androidx.room.ForeignKey

//
// ================================
// Category Entity
// ================================
// Represents a category for transactions (e.g., groceries, salary).
// Can be linked to a user or be globally accessible as a default category.
@Entity(
    tableName = "category_table",
    foreignKeys = [
        // Links category to UserEntity (nullable user_id for global categories)
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"]
        )
    ]
)
data class CategoryEntity (
    @PrimaryKey(autoGenerate = true) val category_id: Int,
    val user_id: Int?, // Nullable to allow for default categories to be globally accessible
    val name: String,
    val type: String,
    val colour: Int,
    val icon: Int,
)

// ================================ //
@Entity(tableName = "category_colors_table")
data class CategoryColorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val colorValue: Int, // Color resource ID
    val hexCode: String
)

@Entity(tableName = "category_icons_table")
data class CategoryIconEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val iconResourceId: Int, // Drawable resource ID
)
