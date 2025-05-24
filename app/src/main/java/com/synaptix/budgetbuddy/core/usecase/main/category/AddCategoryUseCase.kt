package com.synaptix.budgetbuddy.core.usecase.main.category

import android.util.Log
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: FirestoreCategoryRepository
) {
    sealed class AddCategoryResult {
        data class Success(val categoryId: String) : AddCategoryResult()
        data class Error(val message: String) : AddCategoryResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to add a new category
    suspend fun execute(newCategory: Category): AddCategoryResult {
        val newCategoryDTO = newCategory.toDTO()

        // Attempt to create the category
        return try {
            when (val result = categoryRepository.createCategory(newCategoryDTO)) {
                is Result.Success -> {
                    Log.d("AddCategoryUseCase", "Category added successfully: ${result.data}")
                    AddCategoryResult.Success(result.data)
                }
                is Result.Error -> {
                    Log.e("AddCategoryUseCase", "Error adding category: ${result.exception.message}")
                    AddCategoryResult.Error("Failed to add category: ${result.exception.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("AddCategoryUseCase", "Exception while adding category: ${e.message}")
            AddCategoryResult.Error("Failed to add category: ${e.message}")
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\