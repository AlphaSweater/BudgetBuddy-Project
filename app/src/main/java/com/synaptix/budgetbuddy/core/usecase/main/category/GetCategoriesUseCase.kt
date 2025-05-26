package com.synaptix.budgetbuddy.core.usecase.main.category

import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// UseCase class for retrieving categories associated with a user
class GetCategoriesUseCase @Inject constructor(
    // Injecting the CategoryRepository to handle the category-related operations
    private val categoryRepository: FirestoreCategoryRepository,
    private val userRepository: FirestoreUserRepository
) {
    sealed class GetCategoriesResult {
        data class Success(val categories: List<Category>) : GetCategoriesResult()
        data class Error(val message: String) : GetCategoriesResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to fetch the categories for the specified user
    suspend fun execute(userId: String): GetCategoriesResult {
        // Input validation
        if (userId.isEmpty()) {
            return GetCategoriesResult.Error("Invalid user ID")
        }

        // Attempt to retrieve categories from the repository
        return try {
            // First get the user data
            val userResult = userRepository.getUserProfile(userId)
            val user = when (userResult) {
                is Result.Success -> userResult.data!!.toDomain()
                is Result.Error -> return GetCategoriesResult.Error("Failed to get user data: ${userResult.exception.message}")
            }

            // Then get the categories
            val categoryResult = categoryRepository.getCategoriesForUser(userId)
            val categories = when (categoryResult) {
                is Result.Success -> categoryResult.data.map { it.toDomain(user) }
                is Result.Error -> return GetCategoriesResult.Error("Failed to get categories: ${categoryResult.exception.message}")
            }

            println("Retrieved ${categories.size} categories for user $userId")
            GetCategoriesResult.Success(categories)
        } catch (e: Exception) {
            println("Failed to get categories: ${e.message}")
            GetCategoriesResult.Error("Failed to get categories: ${e.message}")
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\