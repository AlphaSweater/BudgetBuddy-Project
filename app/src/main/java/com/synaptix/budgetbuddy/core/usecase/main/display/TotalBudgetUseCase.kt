package com.synaptix.budgetbuddy.core.usecase.main.display

import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TotalBudgetUseCase @Inject constructor(
    private val budgetRepository: FirestoreBudgetRepository,
    private val userRepository: FirestoreUserRepository,
    private val categoryRepository: FirestoreCategoryRepository
) {

    suspend fun execute(userId: String): Double {
        // Ensure userId is not null or empty
        if (userId.isEmpty()) {
            throw IllegalArgumentException("Invalid user ID")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch user profile based on provided userid
        val userResult = userRepository.getUserProfile(userId).first()
        val user = when (userResult) {
            is Result.Success -> userResult.data?.toDomain()
            is Result.Error -> throw Exception("Failed to get user data: ${userResult.exception.message}")
        }
        //checks to see if user object is not null
        if (user == null) {
            throw Exception("User not found")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch categories based on user id
        val categorieResult = categoryRepository.getCategoriesForUser(userId).first()
        val categories = when (categorieResult) {
            is Result.Success -> categorieResult.data.map { it.toDomain(user) }
            is Result.Error -> throw Exception("Failed to get categories: ${categorieResult.exception.message}")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch budgets for the user
        val budgetResult = budgetRepository.getBudgetsForUser(userId).first()
        val budgets = when (budgetResult) {
            is Result.Success -> budgetResult.data.map { it.toDomain(user, categories) }
            is Result.Error -> throw Exception("Failed to get budgets: ${budgetResult.exception.message}")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Calculate the total budget by summing up the amounts of all budgets
        return calculateTotalBudget(budgets)
    }

    fun calculateTotalBudget(budgets: List<Budget>): Double {
        // Calculate the total budget by summing up the amounts of all budgets
        return budgets.sumOf { it.amount }
    }

}