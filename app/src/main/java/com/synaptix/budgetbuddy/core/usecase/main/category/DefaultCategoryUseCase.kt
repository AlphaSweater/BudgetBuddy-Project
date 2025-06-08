package com.synaptix.budgetbuddy.core.usecase.main.category

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

class DefaultCategoryUseCase @Inject constructor(
    private val categoryRepository: FirestoreCategoryRepository
) {
    sealed class DefaultCategoryResult {
        object Success : DefaultCategoryResult()
        data class Error(val message: String) : DefaultCategoryResult()
    }

    fun populateForUser(user: User): Flow<DefaultCategoryResult> = flow {
        try {
            val defaultCategories = getDefaultCategories(user)

            for (category in defaultCategories) {
                when (val result = categoryRepository.createCategory(user.id, category.toDTO())) {
                    is Result.Success -> {
                        Log.d("DefaultCategoryUseCase", "Added category: ${category.name}")
                    }
                    is Result.Error -> {
                        Log.e("DefaultCategoryUseCase", "Failed to add ${category.name}: ${result.exception.message}")
                        emit(DefaultCategoryResult.Error("Failed to add category: ${category.name}"))
                        return@flow
                    }
                }
            }

            emit(DefaultCategoryResult.Success)

        } catch (e: Exception) {
            Log.e("DefaultCategoryUseCase", "Error populating default categories: ${e.message}")
            emit(DefaultCategoryResult.Error("Unexpected error: ${e.message}"))
        }
    }

    private fun getDefaultCategories(user: User): List<Category> {
        return listOf(
            Category.new(user, "Food", "Expense", icon = R.drawable.ic_cat_food, color = R.color.cat_dark_purple),
            Category.new(user, "Transport", "Expense", icon = R.drawable.ic_cat_vehicle, color = R.color.cat_yellow),
            Category.new(user, "Utilities", "Expense", icon = R.drawable.ic_cat_fuel, color = R.color.cat_dark_brown),
            Category.new(user, "Entertainment", "Expense", icon = R.drawable.ic_cat_art, color = R.color.cat_dark_purple),
            Category.new(user, "Salary", "Income", icon = R.drawable.ic_cat_income, color = R.color.cat_orange),
            Category.new(user, "Freelance", "Income", icon = R.drawable.ic_cat_electronics, color = R.color.cat_dark_pink)
        )
    }
}