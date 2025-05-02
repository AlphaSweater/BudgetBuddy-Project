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

import java.io.Serializable

//======================================================================================
// Data Model: CategoryIn
// Represents the input data when creating or updating a category.
// Implements Serializable for safe passing between Android components.
//======================================================================================
data class CategoryIn (
    val categoryId: Int = 0,
    val userId: Int?,          // Associated user ID (nullable)
    val categoryName: String,  // Name of the category
    val categoryType: String,  // Type (e.g., Income or Expense)
    val categoryIcon: Int,     // Resource ID for the category icon
    val categoryColor: Int     // Resource ID or color integer for display
) : Serializable

//======================================================================================
// Data Model: Category
// Represents the full category entity including related user.
//======================================================================================
data class Category(
    val categoryId: Int = 0,
    val user: User?,           // Associated user object (nullable)
    val categoryName: String,  // Name of the category
    val categoryType: String,  // Type (e.g., Income or Expense)
    val categoryIcon: Int,     // Icon for display
    val categoryColor: Int     // Color associated with the category
)
