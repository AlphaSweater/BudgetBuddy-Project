package com.synaptix.budgetbuddy.core.usecase.main.budget

import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class CalculateBudgetSpentUseCase @Inject constructor(
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

    /**
     * Calculates the total spent amount for a budget based on its categories and transactions
     * within the current month.
     */
    suspend fun execute(budget: Budget): Result<Double> {
        return try {
            // Get the current month's date range
            val (startOfMonth, endOfMonth) = getCurrentMonthRange()

            // Get all transactions for each category in the budget within current month
            val categorySpentAmounts = budget.categories.map { category ->
                when (val result = transactionRepository.getTotalAmountForCategoryInDateRange(
                    userId = budget.user.id,
                    categoryId = category.id,
                    startDate = startOfMonth,
                    endDate = endOfMonth
                )) {
                    is Result.Success -> result.data
                    is Result.Error -> return Result.Error(result.exception)
                }
            }

            // Sum up all spent amounts
            val totalSpent = categorySpentAmounts.sum()
            Result.Success(totalSpent)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Observes the spent amount for a budget, updating whenever transactions change.
     * Only includes transactions from the current month.
     */
    fun observeSpentAmount(budget: Budget): Flow<Result<Double>> {
        val (startOfMonth, endOfMonth) = getCurrentMonthRange()
        
        return transactionRepository.observeTransactionsForUserInDateRange(
            userId = budget.user.id,
            startDate = startOfMonth,
            endDate = endOfMonth
        ).map { transactions ->
            try {
                // Filter transactions by category
                val relevantTransactions = transactions.filter { transaction ->
                    budget.categories.any { it.id == transaction.categoryId }
                }
                
                // Sum up the amounts
                val totalSpent = relevantTransactions.sumOf { it.amount }
                Result.Success(totalSpent)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
} 