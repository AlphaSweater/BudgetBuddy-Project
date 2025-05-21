package com.synaptix.budgetbuddy.core.usecase.main.budget

import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.repository.BudgetRepository
import com.synaptix.budgetbuddy.core.repository.CategoryRepository
import com.synaptix.budgetbuddy.core.repository.TransactionRepository
import com.synaptix.budgetbuddy.data.repository.BudgetRepository
import com.synaptix.budgetbuddy.data.repository.CategoryRepository
import com.synaptix.budgetbuddy.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Use case for retrieving and formatting budget report data.
 * This use case combines data from multiple repositories and formats it for display.
 */
class GetBudgetReportUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    /**
     * Data class representing the complete budget report
     */
    data class BudgetReport(
        val budget: Budget,
        val transactions: List<BudgetListItems>,
        val categories: List<BudgetListItems>
    )

    /**
     * Retrieves the complete budget report for a given budget ID
     * @param budgetId The ID of the budget to get the report for
     * @return Flow of BudgetReport containing all necessary data
     */
    operator fun invoke(userId: Int, budgetId: Int): BudgetReport {
        val budget = budgetRepository.getBudgetById(budgetId)
        val transactions = transactionRepository.getTransactionsForUserBudget(userId, budgetId)
    }

    private fun formatTransactions(transactions: List<Transaction>): List<BudgetListItems> {
        if (transactions.isEmpty()) return emptyList()

        val result = mutableListOf<BudgetListItems>()
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        
        // Group transactions by date
        val groupedTransactions = transactions
            .sortedByDescending { it.date }
            .groupBy { it.date }
        
        groupedTransactions.forEach { (date, dayTransactions) ->
            // Add date header
            result.add(
                BudgetListItems.BudgetDateHeader(
                    dateNumber = dateFormat.format(date),
                    relativeDate = getRelativeDate(date),
                    monthYearDate = monthYearFormat.format(date),
                    amountTotal = dayTransactions.sumOf { it.amount }
                )
            )
            
            // Add transactions for this date
            dayTransactions.forEach { transaction ->
                result.add(
                    BudgetListItems.BudgetTransactionItem(
                        transaction = transaction,
                        categoryName = transaction.category?.categoryName ?: "Uncategorized",
                        categoryIcon = transaction.category?.categoryIcon ?: R.drawable.ic_money_24,
                        categoryColour = transaction.category?.categoryColor ?: R.color.cat_light_blue,
                        relativeDate = getRelativeDate(date)
                    )
                )
            }
        }
        
        return result
    }

    private fun formatCategories(
        categories: List<Category>,
        transactions: List<Transaction>
    ): List<BudgetListItems> {
        if (categories.isEmpty()) return emptyList()

        return categories
            .sortedByDescending { category ->
                transactions
                    .filter { it.category?.id == category.id }
                    .sumOf { it.amount }
            }
            .map { category ->
                val categoryTransactions = transactions.filter { it.category?.id == category.id }
                BudgetListItems.BudgetCategoryItem(
                    category = category,
                    transactionCount = categoryTransactions.size,
                    amount = "R ${categoryTransactions.sumOf { it.amount }}",
                    relativeDate = getRelativeDate(categoryTransactions.maxOfOrNull { it.date } ?: Date())
                )
            }
    }

    private fun getRelativeDate(date: Date): String {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time
        
        return when {
            date == today -> "Today"
            date == yesterday -> "Yesterday"
            else -> SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
        }
    }
} 