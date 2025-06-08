package com.synaptix.budgetbuddy.core.usecase.main.category

import android.util.Log
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: FirestoreCategoryRepository
) {
    sealed class UpdateCategoryResult {
        data class Success(val categoryId: String) : UpdateCategoryResult()
        data class Error(val message: String) : UpdateCategoryResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to update an existing category
    fun execute(updatedCategory: Category): Flow<UpdateCategoryResult> = flow {
        try {
            val updatedCategoryDTO = updatedCategory.toDTO()

            // Attempt to update the category
            when (val result = categoryRepository.updateCategory(updatedCategory.user!!.id, updatedCategoryDTO)) {
                is Result.Success -> {
                    Log.d("UpdateCategoryUseCase", "Category updated successfully: ${updatedCategory.id}")
                    emit(UpdateCategoryResult.Success(updatedCategory.id))
                }
                is Result.Error -> {
                    Log.e("UpdateCategoryUseCase", "Error updating category: ${result.exception.message}")
                    emit(UpdateCategoryResult.Error("Failed to update category: ${result.exception.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateCategoryUseCase", "Exception while updating category: ${e.message}")
            emit(UpdateCategoryResult.Error("Failed to update category: ${e.message}"))
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\