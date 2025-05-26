package com.synaptix.budgetbuddy.core.usecase.main.budget

import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.usecase.main.budget.GetBudgetsUseCase.GetBudgetsResult.*
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.model.BudgetDTO
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetBudgetsUseCase @Inject constructor(
    private val budgetRepository: FirestoreBudgetRepository,
    private val userRepository: FirestoreUserRepository,
    private val categoryRepository: FirestoreCategoryRepository

) {
    sealed class GetBudgetsResult {
        data class Success(val budgets: List<Budget>) : GetBudgetsResult()
        data class Error(val message: String) : GetBudgetsResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to fetch budgets for the specified user
    suspend fun execute(userId: String): GetBudgetsResult {
        if (userId.isEmpty()) {
            return Error("Invalid user ID")
        }

        return try {
            val userResult = userRepository.getUserProfile(userId).first()
            val user = when (userResult) {
                is Result.Success -> userResult.data?.toDomain()
                is Result.Error -> return Error("Failed to get user data: ${userResult.exception.message}")
            }

            val budgetResult = budgetRepository.getBudgetsForUser(userId).first()
            val budgetDTOs = when (budgetResult) {
                is Result.Success -> budgetResult.data
                is Result.Error -> return Error("Failed to get budgets: ${budgetResult.exception.message}")
            }

            val budgets = budgetDTOs.mapNotNull { budgetDTO ->
                when (val result = getBudgetData(budgetDTO)) {
                    is Result.Success -> result.data
                    is Result.Error -> {
                        println("Failed to get budget data: ${result.exception.message}")
                        null
                    }
                }
            }

            println("Retrieved ${budgets.size} budgets for user $userId")
            Success(budgets)
        } catch (e: Exception) {
            println("Failed to get budgets: ${e.message}")
            Error("Failed to get budgets: ${e.message}")
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Helper function to get full budget data including user and categories
    private suspend fun getBudgetData(budgetDTO: BudgetDTO): Result<Budget> {
        val user = when (val result = userRepository.getUserProfile(budgetDTO.userId).first()) {
            is Result.Success -> result.data?.toDomain()
            is Result.Error -> return Result.Error(result.exception)
        } ?: return Result.Error(Exception("User not found"))

        val categories = when (val result = categoryRepository.getCategoriesByIds(budgetDTO.categoryIds).first()) {
            is Result.Success -> result.data.map { it.toDomain(user) }
            is Result.Error -> return Result.Error(result.exception)
        }

        return Result.Success(budgetDTO.toDomain(user, categories))
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\