package com.synaptix.budgetbuddy.core.usecase.main.budget

import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.core.util.DateUtil
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.model.TransactionDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreTransactionRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Unified use case for calculating budget-related values.
 * Handles both total budget calculations and individual budget calculations.
 */
class BudgetCalculationUseCase @Inject constructor(
    private val budgetRepository: FirestoreBudgetRepository,
    private val userRepository: FirestoreUserRepository,
    private val categoryRepository: FirestoreCategoryRepository,
    private val transactionRepository: FirestoreTransactionRepository
) {
    /**
     * Calculates the total budget summary for a user
     * @param userId The ID of the user to calculate budget for
     * @return TotalBudgetSummary containing total budgeted and spent amounts
     */
    suspend fun calculateTotalBudgetsSummary(userId: String): Result<BudgetListItems.TotalBudgetsSummary> = try {
        require(userId.isNotEmpty()) { "Invalid user ID" }

        val user = userRepository.getUserProfile(userId)
            .let { result ->
                when (result) {
                    is Result.Success -> result.data?.toDomain() 
                        ?: throw Exception("User not found")
                    is Result.Error -> throw Exception("Failed to get user data: ${result.exception.message}")
                }
            }

        val categories = categoryRepository.getCategoriesForUser(userId)
            .let { result ->
                when (result) {
                    is Result.Success -> result.data.map { it.toDomain(user) }
                    is Result.Error -> throw Exception("Failed to get categories: ${result.exception.message}")
                }
            }

        val budgets = budgetRepository.getBudgetsForUser(userId)
            .let { result ->
                when (result) {
                    is Result.Success -> result.data.map { it.toDomain(user, categories) }
                    is Result.Error -> throw Exception("Failed to get budgets: ${result.exception.message}")
                }
            }

        val (startOfMonth, endOfMonth) = DateUtil.getCurrentMonthRange()
        val transactions = transactionRepository.getTransactionsForUserInDateRange(
            userId = userId,
            startDate = startOfMonth,
            endDate = endOfMonth
        ).let { result ->
            when (result) {
                is Result.Success -> result.data
                is Result.Error -> throw Exception("Failed to get transactions: ${result.exception.message}")
            }
        }

        Result.Success(calculateTotalSummary(budgets, transactions))
    } catch (e: Exception) {
        Result.Error(e)
    }

    /**
     * Calculates the spent amount for a specific budget
     * @param budget The budget to calculate spent amount for
     * @return BudgetSpentSummary containing the budget details and spent amount
     */
    suspend fun calculateBudgetSpent(budget: Budget): Result<BudgetListItems.BudgetBudgetItem> = try {
        val (startOfMonth, endOfMonth) = DateUtil.getCurrentMonthRange()
        
        val transactions = transactionRepository.getTransactionsForUserInDateRange(
            userId = budget.user.id,
            startDate = startOfMonth,
            endDate = endOfMonth
        ).let { result ->
            when (result) {
                is Result.Success -> result.data
                is Result.Error -> throw Exception("Failed to get transactions: ${result.exception.message}")
            }
        }

        val budgetCategoryIds = budget.categories.map { it.id }.toSet()
        val spentAmount = transactions
            .filter { it.categoryId in budgetCategoryIds }
            .sumOf { it.amount }

        Result.Success(
            BudgetListItems.BudgetBudgetItem(
                budget = budget,
                budgetedAmount = budget.amount,
                spentAmount = spentAmount,
                remainingAmount = budget.amount - spentAmount
            )
        )
    } catch (e: Exception) {
        Result.Error(e)
    }

    /**
     * Observes the total budget summary for a user
     * @param userId The ID of the user to observe budget for
     * @return Flow of Result<TotalBudgetSummary> that updates in real-time
     */
    fun observeTotalBudgetsSummary(userId: String): Flow<Result<BudgetListItems.TotalBudgetsSummary>> {
        if (userId.isEmpty()) {
            return flow { emit(Result.Error(IllegalArgumentException("Invalid user ID"))) }
        }

        val (startOfMonth, endOfMonth) = DateUtil.getCurrentMonthRange()

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
                // Create a map of category IDs to categories for quick lookup
                val categoryMap = categories.map { it.toDomain(domainUser) }.associateBy { it.id }
                // Map budgets using only the categories that are actually in the budget document
                val domainBudgets = budgets.map { budget ->
                    budget.toDomain(domainUser, budget.categoryIds.mapNotNull { categoryMap[it] })
                }

                Result.Success(calculateTotalSummary(domainBudgets, transactions))
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged { old, new -> 
            old is Result.Success && new is Result.Success && 
            old.data.totalBudgeted == new.data.totalBudgeted &&
            old.data.totalSpent == new.data.totalSpent 
        }
        .buffer()
    }

    /**
     * Observes the spent amount for a specific budget
     * @param budget The budget to observe spent amount for
     * @return Flow of Result<BudgetSpentSummary> that updates in real-time
     */
    fun observeBudgetSpent(budget: Budget): Flow<Result<BudgetListItems.BudgetBudgetItem>> {
        val (startOfMonth, endOfMonth) = DateUtil.getCurrentMonthRange()
        val budgetCategoryIds = budget.categories.map { it.id }.toSet()
        
        return transactionRepository.observeTransactionsForUserInDateRange(
            userId = budget.user.id,
            startDate = startOfMonth,
            endDate = endOfMonth
        ).map { transactions ->
            try {
                val spentAmount = transactions
                    .filter { it.categoryId in budgetCategoryIds }
                    .sumOf { it.amount }

                Result.Success(
                    BudgetListItems.BudgetBudgetItem(
                        budget = budget,
                        budgetedAmount = budget.amount,
                        spentAmount = spentAmount,
                        remainingAmount = budget.amount - spentAmount
                    )
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    private fun calculateTotalSummary(budgets: List<Budget>, transactions: List<TransactionDTO>): BudgetListItems.TotalBudgetsSummary {
        val totalBudgets = budgets.size
        val totalBudgeted = budgets.sumOf { it.amount }
        
        // Get only the categories that are actually stored in the budget documents
        val budgetCategories = budgets.flatMap { budget ->
            // Only use the categories that are actually in this budget
            budget.categories
        }.distinctBy { it.id }
        
        // Then filter to only expense categories that are in budgets
        val budgetExpenseCategories = budgetCategories.filter { it.type == "expense" }
        
        // Get the IDs of these categories
        val budgetExpenseCategoryIds = budgetExpenseCategories.map { it.id }.toSet()

        println("\n=== BUDGETS DETAIL ===")
        budgets.forEach { budget ->
            println("\nBudget: ${budget.name}")
            println("Amount: ${budget.amount}")
            println("Categories in this budget:")
            budget.categories.forEach { category ->
                println("  - Category: ${category.name}")
                println("    ID: ${category.id}")
                println("    Type: ${category.type}")
            }
        }

        println("\n=== BUDGET EXPENSE CATEGORIES ===")
        println("Total unique expense categories in budgets: ${budgetExpenseCategories.size}")
        budgetExpenseCategories.forEach { category ->
            println("  - ${category.name} (${category.id})")
        }
        
        println("\n=== ALL TRANSACTIONS ===")
        println("Total transactions before filtering: ${transactions.size}")
        transactions.forEach { transaction ->
            val category = budgetCategories.find { it.id == transaction.categoryId }
            println("\nTransaction:")
            println("  Amount: ${transaction.amount}")
            println("  Category ID: ${transaction.categoryId}")
            println("  Category Name: ${category?.name ?: "NOT FOUND IN BUDGETS"}")
            println("  Category Type: ${category?.type ?: "NOT FOUND IN BUDGETS"}")
            println("  Is in budget expense categories: ${transaction.categoryId in budgetExpenseCategoryIds}")
        }

        // Filter transactions to only include those from expense categories that are in budgets
        val filteredTransactions = transactions.filter { transaction ->
            val isIncluded = transaction.categoryId in budgetExpenseCategoryIds
            if (!isIncluded) {
                val category = budgetCategories.find { it.id == transaction.categoryId }
                println("\nFiltered out transaction:")
                println("  Amount: ${transaction.amount}")
                println("  Category ID: ${transaction.categoryId}")
                println("  Category Name: ${category?.name ?: "NOT FOUND IN BUDGETS"}")
                println("  Category Type: ${category?.type ?: "NOT FOUND IN BUDGETS"}")
            }
            isIncluded
        }

        println("\n=== FILTERED TRANSACTIONS ===")
        println("Total transactions after filtering: ${filteredTransactions.size}")
        filteredTransactions.forEach { transaction ->
            val category = budgetCategories.find { it.id == transaction.categoryId }
            println("\nTransaction:")
            println("  Amount: ${transaction.amount}")
            println("  Category ID: ${transaction.categoryId}")
            println("  Category Name: ${category?.name ?: "NOT FOUND IN BUDGETS"}")
            println("  Category Type: ${category?.type ?: "NOT FOUND IN BUDGETS"}")
        }

        val totalSpent = filteredTransactions.sumOf { it.amount }
        val totalRemaining = totalBudgeted - totalSpent

        return BudgetListItems.TotalBudgetsSummary(totalBudgets, totalBudgeted, totalSpent, totalRemaining)
    }
} 