package com.synaptix.budgetbuddy.core.usecase.main.defaults

import android.util.Log
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class InsertDefaultsUseCase @Inject constructor(
    private val categoryRepository: FirestoreCategoryRepository
) {
    sealed class InsertResult {
        object Success : InsertResult()
        data class Error(val message: String) : InsertResult()
    }

    fun execute(user: User): Flow<InsertResult> = flow {
        try {
            insertDefaultCategories(user)
            // insertDefaultLabels(user) <- for future use
            emit(InsertResult.Success)
        } catch (e: Exception) {
            Log.e("InsertDefaultsUseCase", "Error inserting defaults: ${e.message}")
            emit(InsertResult.Error(e.message ?: "Unknown error"))
        }
    }

    // --- Private helper methods ---

    private suspend fun insertDefaultCategories(user: User) {
        val defaultCategories = listOf(
            Category.new(user, "Food", "expense", R.drawable.ic_cat_food, R.color.cat_dark_purple),
            Category.new(user, "Transport", "expense", R.drawable.ic_cat_vehicle, R.color.cat_yellow),
            Category.new(user, "Utilities", "expense", R.drawable.ic_cat_fuel, R.color.cat_dark_brown),
            Category.new(user, "Entertainment", "expense", R.drawable.ic_cat_art, R.color.cat_dark_purple),
            Category.new(user, "Salary", "income", R.drawable.ic_cat_income, R.color.cat_orange),
            Category.new(user, "Freelance", "income", R.drawable.ic_cat_electronics, R.color.cat_dark_pink)
        )

        for (category in defaultCategories) {
            val result = categoryRepository.createCategory(user.id, category.toDTO(isDefault = true))
            if (result is Result.Error) {
                throw Exception("Failed to add category: ${category.name}")
            }
        }
    }

    // Example placeholder for future:
    /*
    private suspend fun insertDefaultLabels(user: User) {
        // Implementation will go here later
    }
    */
}
