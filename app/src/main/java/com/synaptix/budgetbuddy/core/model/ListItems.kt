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

sealed class HomeItems {
    /**
     * Represents a summary of transactions.
     * Contains total income, expense, and balance information.
     */
    data class TransactionsSummary(
        val totalIncome: Double,
        val totalExpense: Double,
        val balance: Double
    ) : HomeItems()

    /**
     * Represents a category-specific transaction summary.
     * Contains category details and transaction totals.
     */
    data class CategoryTransactionsSummary(
        val category: Category,
        val totalIncome: Double,
        val totalExpense: Double,
        val balance: Double,
        val transactionCount: Int,
        val lastTransactionAt: Long
    ) : HomeItems()
}


/**
 * Sealed class for items used in the home screen.
 * Contains items specific to the home screen display.
 */
sealed class HomeListItems {
    /**
     * Represents a wallet item in the home screen.
     * Maintains the original Wallet object while adding UI-specific data.
     */
    data class HomeWalletItem(
        val wallet: Wallet,
        val walletIcon: Int,
        val relativeDate: String = wallet.formatDate(wallet.lastTransactionAt)
    ) : HomeListItems()

    /**
     * Represents a transaction item in the home screen.
     * Maintains the original Transaction object while adding UI-specific data.
     */
    data class HomeTransactionItem(
        val transaction: Transaction,
        val relativeDate: String
    ) : HomeListItems()

    /**
     * Represents a category item in the home screen.
     * Maintains the original Category object while adding UI-specific data.
     */
    data class HomeCategoryItem(
        val category: Category,
        val transactionCount: Int,
        val amount: String,
        val relativeDate: String
    ) : HomeListItems()
}

/**
 * Sealed class for items used in budget reports.
 * Contains items specific to budget report display.
 */
sealed class BudgetListItems {

    /**
     * Represents the total budgets values
     */
    data class TotalBudgetsSummary(
        val totalBudgets: Int,
        val totalBudgeted: Double,
        val totalSpent: Double,
        val totalRemaining: Double
    ) :BudgetListItems()

    /**
     * Represents a budget item in the budget report.
     * Maintains the original Budget object while adding UI-specific data.
     */
    data class BudgetBudgetItem(
        // The original Budget object
        val budget: Budget,

        val budgetedAmount: Double,
        val spentAmount: Double,
        val remainingAmount: Double,
    ) : BudgetListItems()

    /**
     * Represents a date header in the budget report.
     * Contains UI-specific date formatting and total amount.
     */
    data class BudgetDateHeader(
        // The date number (e.g., 01, 02, etc.)
        val dateNumber: String,

        // The relative date (e.g., 'Yesterday', 'Today')
        val relativeDate: String,

        // The month and year representation of the date
        val monthYearDate: String,

        // The total amount for the given date
        val amountTotal: Double
    ) : BudgetListItems()

    /**
     * Represents a transaction item in the budget report.
     * Maintains the original Transaction object while adding UI-specific data.
     */
    data class BudgetTransactionItem(
        val transaction: Transaction,
        // The name of the category the transaction belongs to
        val categoryName: String,

        // The drawable resource ID for the category icon
        val categoryIcon: Int,

        // The color associated with the category
        val categoryColour: Int,

        // The relative date of the transaction
        val relativeDate: String
    ) : BudgetListItems()

    /**
     * Represents a category summary in the budget report.
     * Maintains the original Category object while adding UI-specific data.
     */
    data class BudgetCategoryItem(
        val category: Category,
        // The number of transactions in this category
        val transactionCount: Int,

        // The total amount in this category, as a string
        val amount: String,

        // The relative date for the category item
        val relativeDate: String,
    ) : BudgetListItems()

    /**
     * Represents a wallet item in the budget report.
     * Maintains the original Wallet object while adding UI-specific data.
     */
    data class BudgetWalletItem(
        val wallet: Wallet,
        // The name of the wallet
        val walletName: String,

        // The drawable resource ID for the wallet icon
        val walletIcon: Int,

        // The balance of the wallet
        var walletBalance: Double,

        // The relative date for the wallet item
        val relativeDate: String
    ) : BudgetListItems()

    /**
     * Represents a label item in the budget report.
     * Maintains the original Label object while adding UI-specific data.
     */
    data class BudgetLabelItem(
        val label: Label,
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
    ) : BudgetListItems()
}

/**
 * Sealed class for items used in wallet management.
 * Contains items specific to wallet display and management.
 */
sealed class WalletListItems {
    /**
     * Represents a wallet item in wallet management screens.
     * Maintains the original Wallet object while adding UI-specific data.
     */
    data class WalletItem(
        val wallet: Wallet,
        val walletIcon: Int,
        val relativeDate: String
    ) : WalletListItems()
}

/**
 * Sealed class for items used in category management.
 * Contains items specific to category display and management.
 */
sealed class CategoryListItems {
    /**
     * Represents a category item in category management screens.
     * Maintains the original Category object while adding UI-specific data.
     */
    data class CategoryItem(
        val category: Category,
        val transactionCount: Int,
        val amount: String,
        val relativeDate: String
    ) : CategoryListItems()
}

/**
 * Sealed class for items used in transaction management.
 * Contains items specific to transaction display and management.
 */
sealed class TransactionListItems {
    /**
     * Represents a transaction item in transaction management screens.
     * Maintains the original Transaction object while adding UI-specific data.
     */
    data class TransactionItem(
        val transaction: Transaction,
        val categoryName: String,
        val categoryIcon: Int,
        val categoryColour: Int,
        val relativeDate: String
    ) : TransactionListItems()
}

/**
 * Sealed class for items used in label management.
 * Contains items specific to label display and management.
 */
sealed class LabelListItems {
    /**
     * Represents a label item in label management screens.
     * Maintains the original Label object while adding UI-specific data.
     */
    data class LabelItem(
        val label: Label,
        val labelIcon: Int,
        val labelColour: Int,
        val transactionCount: Int,
        val amount: String,
        val relativeDate: String
    ) : LabelListItems()
}
