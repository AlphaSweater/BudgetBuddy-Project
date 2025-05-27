package com.synaptix.budgetbuddy.core.usecase.main.category

import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    fun execute(userId: String): Flow<GetCategoriesResult> {
        if (userId.isEmpty()) {
            return kotlinx.coroutines.flow.flow { 
                emit(GetCategoriesResult.Error("Invalid user ID")) 
            }
        }

        return combine(
            userRepository.observeUserProfile(userId),
            categoryRepository.observeCategoriesForUser(userId)
        ) { user, categories ->
            when (user) {
                null -> GetCategoriesResult.Error("User not found")
                else -> {
                    val domainUser = user.toDomain()
                    GetCategoriesResult.Success(categories.map { it.toDomain(domainUser) })
                }
            }
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\