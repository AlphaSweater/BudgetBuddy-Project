//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.core.model

// Sealed class representing different types of items in the budget report
sealed class BudgetReportListItems {

    // Data class for a date header item in the budget report
    data class DateHeader(
        // The date number (e.g., 01, 02, etc.)
        val dateNumber: String,

        // The relative date (e.g., 'Yesterday', 'Today')
        val relativeDate: String,

        // The month and year representation of the date
        val monthYearDate: String,

        // The total amount for the given date
        val amountTotal: Double
    ) : BudgetReportListItems()

    // Data class for a transaction item in the budget report
    data class TransactionItem(
        // The name of the category the transaction belongs to
        val categoryName: String,

        // The drawable resource ID for the category icon
        val categoryIcon: Int,

        // The color associated with the category
        val categoryColour: Int,

        // The amount for the transaction
        val amount: Double,

        // The name of the wallet used in the transaction
        val walletName: String,

        // The note associated with the transaction, can be null
        val note: String?,

        // The relative date of the transaction
        val relativeDate: String
    ) : BudgetReportListItems()

    // Data class for category items summary in the budget report
    data class CategoryItems(
        // The name of the category
        val categoryName: String,

        // The drawable resource ID for the category icon
        val categoryIcon: Int,

        // The color associated with the category
        val categoryColour: Int,

        // The number of transactions in this category
        val transactionCount: Int,

        // The total amount in this category, as a string
        val amount: String,

        // The relative date for the category item
        val relativeDate: String,
    ) : BudgetReportListItems()

    // Data class for a budget item in the budget report
    data class BudgetItem(
        // The title of the budget item
        val title: String,

        // The status of the budget (e.g., 'Completed', 'Pending')
        val status: String,

        // The drawable resource ID for the budget item icon
        val categoryIcon: Int // Drawable resource ID for the icon (e.g., R.drawable.ic_circle_24)
    )

    // Data class for a wallet item in the budget report
    data class WalletItem(
        // The name of the wallet
        val walletName: String,

        // The drawable resource ID for the wallet icon
        val walletIcon: Int,

        // The balance of the wallet
        var walletBalance: Double
    )

    // Data class for label items in the budget report
    data class LabelItems(
        // The name of the label
        val labelName: String,

        // The drawable resource ID for the label icon
        val labelIcon: Int,

        // The color associated with the label
        val labelColour: Int,

        // The number of transactions under this label
        val transactionCount: Int,

        // The total amount for the label as a string
        val amount: String,

        // The relative date for the label item
        val relativeDate: String,
    ) : BudgetReportListItems()

    data class HomeWalletItem(
        val walletName: String,
        val walletIcon: Int,
        var walletBalance: Double,
        val relativeDate: String,
    ) : BudgetReportListItems()
}
