package com.synaptix.budgetbuddy.core.model

//Structure for the data in the Fragment
sealed class BudgetReportListItems {
    data class DateHeader(
        val dateNumber: String,
        val relativeDate: String,
        val monthYearDate: String,
        val amountTotal: Double
    ) : BudgetReportListItems()

    data class TransactionItem(
        val categoryName: String,
        val categoryIcon: Int,
        val categoryColour: Int,
        val amount: Double,
        val walletName: String,
        val note: String?,
        val relativeDate: String
    ) : BudgetReportListItems()
}
