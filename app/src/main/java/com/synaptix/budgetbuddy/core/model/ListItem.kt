package com.synaptix.budgetbuddy.core.model

//Structure for the data in the Fragment
sealed class ListItem {
    data class TransactionItem(val day: String, val numberDay: String, val monthYear: String, val amount: String) : ListItem()
    data class CategoryItem(val name: String, val transactions: String, val amount: String, val date: String) : ListItem()
}


