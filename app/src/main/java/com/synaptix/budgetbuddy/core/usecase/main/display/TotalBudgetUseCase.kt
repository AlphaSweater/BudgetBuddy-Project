package com.synaptix.budgetbuddy.core.usecase.main.display

import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.model.TransactionDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreTransactionRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
//data class that allows use case to return both amount and spent values for budgets
data class BudgetSummary(
    val totalBudgeted: Double,
    val totalSpent: Double
)

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
class TotalBudgetUseCase @Inject constructor(
    private val budgetRepository: FirestoreBudgetRepository,
    private val userRepository: FirestoreUserRepository,
    private val categoryRepository: FirestoreCategoryRepository,
    private val transactionRepository: FirestoreTransactionRepository
) {

    companion object {
        private fun getCurrentMonthRange(): Pair<Long, Long> {
            val now = LocalDateTime.now()
            val startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0).withMinute(0).withSecond(0).withNano(0)
            val endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth())
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999)

            return Pair(
                startOfMonth.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                endOfMonth.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to calculate the total budget for the specified user
    suspend fun execute(userId: String): BudgetSummary {
        // Ensure userId is not null or empty
        if (userId.isEmpty()) {
            throw IllegalArgumentException("Invalid user ID")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch user profile based on provided userid
        val userResult = userRepository.getUserProfile(userId)
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
        val categorieResult = categoryRepository.getCategoriesForUser(userId)
        val categories = when (categorieResult) {
            is Result.Success -> categorieResult.data.map { it.toDomain(user) }
            is Result.Error -> throw Exception("Failed to get categories: ${categorieResult.exception.message}")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch budgets for the user
        val budgetResult = budgetRepository.getBudgetsForUser(userId)
        val budgets = when (budgetResult) {
            is Result.Success -> budgetResult.data.map { it.toDomain(user, categories) }
            is Result.Error -> throw Exception("Failed to get budgets: ${budgetResult.exception.message}")
        }

        // Get current month's transactions for the user
        val (startOfMonth, endOfMonth) = getCurrentMonthRange()
        val transactionResult = transactionRepository.getTransactionsForUserInDateRange(
            userId = userId,
            startDate = startOfMonth,
            endDate = endOfMonth
        )
        val transactions = when (transactionResult) {
            is Result.Success -> transactionResult.data
            is Result.Error -> throw Exception("Failed to get transactions: ${transactionResult.exception.message}")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Calculate the total budget by summing up the amounts of all budgets
        return calculateBudgetSummary(budgets, transactions)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Function to calculate the total budget summary
    private fun calculateBudgetSummary(budgets: List<Budget>, transactions: List<TransactionDTO>): BudgetSummary {
        val totalBudgeted = budgets.sumOf { it.amount }
        
        // Create a map of category IDs to their budgets for quick lookup
        val categoryToBudgetMap = budgets.flatMap { budget ->
            budget.categories.map { it.id to budget }
        }.toMap()

        // Calculate total spent by processing all transactions at once
        val totalSpent = transactions
            .filter { transaction ->
                // Only include transactions that belong to a budget's category
                categoryToBudgetMap.containsKey(transaction.categoryId)
            }
            .sumOf { it.amount }

        return BudgetSummary(totalBudgeted, totalSpent)
    }

    /**
     * Observes the total budget summary for a user, updating whenever budgets or transactions change
     */
    fun observeBudgetSummary(userId: String): Flow<Result<BudgetSummary>> {
        if (userId.isEmpty()) {
            return flow { emit(Result.Error(IllegalArgumentException("Invalid user ID"))) }
        }

        val (startOfMonth, endOfMonth) = getCurrentMonthRange()

        return combine(
            userRepository.observeUserProfile(userId),
            budgetRepository.observeBudgetsForUser(userId),
            categoryRepository.observeCategoriesForUser(userId),
            transactionRepository.observeTransactionsForUserInDateRange(
                userId = userId,
                startDate = startOfMonth,
                endDate = endOfMonth
            )
        ) { user, budgets, categories, transactions ->
            if (user == null) {
                return@combine Result.Error(Exception("User not found"))
            }

            try {
                val domainUser = user.toDomain()
                val domainCategories = categories.map { it.toDomain(domainUser) }
                val domainBudgets = budgets.map { it.toDomain(domainUser, domainCategories) }

                Result.Success(calculateBudgetSummary(domainBudgets, transactions))
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
        .flowOn(Dispatchers.IO) // Move heavy calculations to IO thread
        .distinctUntilChanged { old, new -> 
            old is Result.Success && new is Result.Success && 
            old.data.totalBudgeted == new.data.totalBudgeted && 
            old.data.totalSpent == new.data.totalSpent 
        } // Only emit if values actually changed
        .buffer() // Handle backpressure
    }

}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\