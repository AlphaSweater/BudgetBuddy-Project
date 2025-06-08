package com.synaptix.budgetbuddy.core.usecase.main.transaction

import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.HomeItems
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.core.util.DateRangeUtil
import com.synaptix.budgetbuddy.data.firebase.model.TransactionDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Unified use case for calculating transaction-related values.
 * Handles both total transaction calculations and category-specific calculations.
 */
class TransactionCalculationsUseCase @Inject constructor(
    private val transactionRepository: FirestoreTransactionRepository,
    private val categoryRepository: FirestoreCategoryRepository
) {
    /**
     * Calculates the total transaction summary for a user
     * @param userId The ID of the user to calculate transactions for
     * @return TransactionsSummary containing total income, expense, and balance
     */
    suspend fun calculateTotalTransactionsSummary(userId: String): Result<HomeItems.TransactionsSummary> = try {
        require(userId.isNotEmpty()) { "Invalid user ID" }

        val (startOfMonth, endOfMonth) = DateRangeUtil.getCurrentMonthRange()
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

        // Get unique category IDs from transactions
        val categoryIds = transactions.map { it.categoryId }.toSet()
        
        // Fetch only the type field for these categories
        val categoryTypes = categoryIds.associateWith { categoryId ->
            when (val result = categoryRepository.getCategoryType(userId, categoryId)) {
                is Result.Success -> result.data
                is Result.Error -> "expense" // Default to expense if there's an error
            }
        }

        Result.Success(calculateTotalSummary(transactions, categoryTypes))
    } catch (e: Exception) {
        Result.Error(e)
    }

    /**
     * Calculates the transaction summary for specific categories
     * @param userId The ID of the user
     * @param categories The categories to calculate summary for
     * @return Map of category ID to CategoryTransactionsSummary
     */
    suspend fun calculateCategoryTransactionsSummary(
        userId: String,
        categories: List<Category>
    ): Result<Map<String, HomeItems.CategoryTransactionsSummary>> = try {
        require(userId.isNotEmpty()) { "Invalid user ID" }

        val (startOfMonth, endOfMonth) = DateRangeUtil.getCurrentMonthRange()
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

        val categorySummaries = categories.associate { category ->
            val categoryTransactions = transactions.filter { it.categoryId == category.id }
            category.id to calculateCategorySummary(category, categoryTransactions)
        }

        Result.Success(categorySummaries)
    } catch (e: Exception) {
        Result.Error(e)
    }

    /**
     * Observes the total transaction summary for a user
     * @param userId The ID of the user to observe transactions for
     * @return Flow of Result<TransactionsSummary> that updates in real-time
     */
    fun observeTotalTransactionsSummary(userId: String): Flow<Result<HomeItems.TransactionsSummary>> {
        if (userId.isEmpty()) {
            return flow { emit(Result.Error(IllegalArgumentException("Invalid user ID"))) }
        }

        val (startOfMonth, endOfMonth) = DateRangeUtil.getCurrentMonthRange()

        return transactionRepository.observeTransactionsForUserInDateRange(
            userId = userId,
            startDate = startOfMonth,
            endDate = endOfMonth
        ).map { transactions ->
            try {
                // Get unique category IDs from transactions
                val categoryIds = transactions.map { it.categoryId }.toSet()
                
                // Fetch only the type field for these categories
                val categoryTypes = categoryIds.associateWith { categoryId ->
                    when (val result = categoryRepository.getCategoryType(userId, categoryId)) {
                        is Result.Success -> result.data
                        is Result.Error -> "expense" // Default to expense if there's an error
                    }
                }

                Result.Success(calculateTotalSummary(transactions, categoryTypes))
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged { old, new ->
            old is Result.Success && new is Result.Success &&
            old.data.totalIncome == new.data.totalIncome &&
            old.data.totalExpense == new.data.totalExpense
        }
        .buffer()
    }

    /**
     * Observes the transaction summary for specific categories
     * @param userId The ID of the user
     * @param categories The categories to observe
     * @return Flow of Result<Map<String, CategoryTransactionsSummary>> that updates in real-time
     */
    fun observeCategoryTransactionsSummary(
        userId: String,
        categories: List<Category>
    ): Flow<Result<Map<String, HomeItems.CategoryTransactionsSummary>>> {
        if (userId.isEmpty()) {
            return flow { emit(Result.Error(IllegalArgumentException("Invalid user ID"))) }
        }

        val (startOfMonth, endOfMonth) = DateRangeUtil.getCurrentMonthRange()

        return transactionRepository.observeTransactionsForUserInDateRange(
            userId = userId,
            startDate = startOfMonth,
            endDate = endOfMonth
        ).map { transactions ->
            try {
                val categorySummaries = categories.associate { category ->
                    val categoryTransactions = transactions.filter { it.categoryId == category.id }
                    category.id to calculateCategorySummary(category, categoryTransactions)
                }
                Result.Success(categorySummaries)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged { old, new ->
            old is Result.Success && new is Result.Success &&
            old.data.all { (categoryId, oldSummary) ->
                val newSummary = new.data[categoryId]
                newSummary != null &&
                oldSummary.totalIncome == newSummary.totalIncome &&
                oldSummary.totalExpense == newSummary.totalExpense
            }
        }
        .buffer()
    }

    private fun calculateTotalSummary(
        transactions: List<TransactionDTO>,
        categoryTypes: Map<String, String>
    ): HomeItems.TransactionsSummary {
        val totalIncome = transactions
            .filter { categoryTypes[it.categoryId] == "income" }
            .sumOf { it.amount }

        val totalExpense = transactions
            .filter { categoryTypes[it.categoryId] == "expense" }
            .sumOf { it.amount }

        val balance = totalIncome - totalExpense

        return HomeItems.TransactionsSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance
        )
    }

    private fun calculateCategorySummary(
        category: Category,
        transactions: List<TransactionDTO>
    ): HomeItems.CategoryTransactionsSummary {
        val totalIncome = transactions
            .filter { category.type == "income" }
            .sumOf { it.amount }

        val totalExpense = transactions
            .filter { category.type == "expense" }
            .sumOf { it.amount }

        val balance = totalIncome - totalExpense

        val lastTransactionAt = transactions.maxByOrNull { it.date }?.date ?: System.currentTimeMillis()

        return HomeItems.CategoryTransactionsSummary(
            category = category,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            transactionCount = transactions.size,
            lastTransactionAt = lastTransactionAt
        )
    }
} 