package com.synaptix.budgetbuddy.core.usecase.main.category

import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.getOrReturn
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import javax.inject.Inject

class GetCategoryUseCase @Inject constructor(
    private val userRepository: FirestoreUserRepository,
    private val categoryRepository: FirestoreCategoryRepository,
) {
    sealed class GetCategoryResult {
        data class Success(val category: Category) : GetCategoryResult()
        data class Error(val message: String) : GetCategoryResult()
    }

    suspend fun execute(userId: String, categoryId: String): GetCategoryResult {
        if (userId.isEmpty()) {
            return GetCategoryResult.Error("Invalid user ID")
        }

        return try {
            val user = userRepository.getUserProfile(userId).getOrReturn {
                return GetCategoryResult.Error("Error fetching user: $it")
            } ?: return GetCategoryResult.Error("User not found")

            val dto = categoryRepository.getCategoryById(userId, categoryId).getOrReturn {
                return GetCategoryResult.Error("Error fetching category: $it")
            } ?: return GetCategoryResult.Error("Category not found")

            val domainUser = user.toDomain()
            val category = dto.toDomain(
                user = domainUser
            )

            GetCategoryResult.Success(category)

        } catch (e: Exception) {
            GetCategoryResult.Error("Unexpected error: ${e.message}")
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\