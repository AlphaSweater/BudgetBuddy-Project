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

    data class CategoryItems(
        val categoryName: String,
        val categoryIcon: Int,
        val categoryColour: Int,
        val transactionCount: Int,
        val amount: String,
        val relativeDate: String,
    ) : BudgetReportListItems()

    data class BudgetItem(
        val id: Int,
        val title: String,
        val status: String,
        val categoryIcon: Int // Drawable resource ID for the icon (e.g., R.drawable.ic_circle_24)
    )

    data class WalletItem(
        val walletName: String,
        val walletIcon: Int,
        var walletBalance: Double
    )

    data class LabelItems(
        val labelName: String,
        val labelIcon: Int,
        val labelColour: Int,
        val transactionCount: Int,
        val amount: String,
        val relativeDate: String,
    ) : BudgetReportListItems()
}
