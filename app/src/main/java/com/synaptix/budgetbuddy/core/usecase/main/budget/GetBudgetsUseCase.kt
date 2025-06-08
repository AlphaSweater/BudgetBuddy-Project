package com.synaptix.budgetbuddy.core.usecase.main.budget

import android.util.Log
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
    fun execute(userId: String): Flow<GetBudgetsResult> {
        if (userId.isEmpty()) {
            return flow { 
                emit(GetBudgetsResult.Error("Invalid user ID")) 
            }
        }

        return combine(
            userRepository.observeUserProfile(userId),
            budgetRepository.observeBudgetsForUser(userId),
            categoryRepository.observeCategoriesForUser(userId)
        ) { user, budgets, categories ->
            if (user == null) {
                return@combine GetBudgetsResult.Error("User not found")
            }

            val domainUser = user.toDomain()
            val categoryMap = categories.associateBy { it.id }

            val fullBudgets = budgets.mapNotNull { dto ->
                try {
                    dto.toDomain(
                        user = domainUser,
                        categories = dto.categoryIds.mapNotNull { categoryMap[it]?.toDomain(domainUser) }
                    )
                } catch (e: Exception) {
                    Log.e("GetBudgetsUseCase", "Error converting a budget DTO to domain: ${e.message}")
                    null
                }
            }

            GetBudgetsResult.Success(fullBudgets)
        }.flowOn(Dispatchers.IO)
    }

    fun executeWithDateRange(userId: String, startDate: Long, endDate: Long): Flow<GetBudgetsResult> {
        if (userId.isEmpty()) {
            return flow {
                emit(GetBudgetsResult.Error("Invalid user ID"))
            }
        }
        if (startDate > endDate) {
            return flow {
                emit(GetBudgetsResult.Error("Invalid date range"))
            }
        }

        return combine(
            userRepository.observeUserProfile(userId),
            budgetRepository.observeBudgetsForUser(userId),
            categoryRepository.observeCategoriesForUser(userId)
        ) { user, budgets, categories ->
            if (user == null) {
                return@combine GetBudgetsResult.Error("User not found")
            }

            val domainUser = user.toDomain()
            val categoryMap = categories.associateBy { it.id }

            val fullBudgets = budgets.mapNotNull { dto ->
                try {
                    dto.toDomain(
                        user = domainUser,
                        categories = dto.categoryIds.mapNotNull { categoryMap[it]?.toDomain(domainUser) }
                    )
                } catch (e: Exception) {
                    Log.e("GetBudgetsUseCase", "Error converting a budget DTO to domain: ${e.message}")
                    null
                }
            }

            GetBudgetsResult.Success(fullBudgets)
        }.flowOn(Dispatchers.IO)
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\