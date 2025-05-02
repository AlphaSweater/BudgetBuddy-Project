package com.synaptix.budgetbuddy.data._default

import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.data.entity.CategoryEntity

//object list of default categories for the database
//null User ID makes default categories global and accessible by all users
object CategoryDefualts {
    val defaultCategories = listOf(
        CategoryEntity(0, null, "Food", "expense", R.color.cat_dark_pink, R.drawable.baseline_fastfood_24),
        CategoryEntity(0, null, "Transport", "expense", R.color.cat_yellow, R.drawable.baseline_local_gas_station_24),
        CategoryEntity(0, null, "HealthCare", "expense", R.color.cat_gold, R.drawable.ic_add_alert_24),
        CategoryEntity(0, null, "Beauty", "expense", R.color.cat_dark_purple, R.drawable.baseline_palette_24),
        CategoryEntity(0, null, "Bills & Fees", "expense", R.color.cat_light_green, R.drawable.baseline_savings_24),
        CategoryEntity(0, null, "Education", "expense", R.color.cat_yellow, R.drawable.baseline_school_24),
        CategoryEntity(0, null, "Entertainment", "expense", R.color.cat_light_purple, R.drawable.baseline_theater_comedy_24),
        CategoryEntity(0, null, "Family & Friends", "expense", R.color.cat_dark_blue, R.drawable.baseline_escalator_warning_24),
        CategoryEntity(0, null, "Groceries", "expense", R.color.cat_light_blue, R.drawable.baseline_shopping_bag_24),
        CategoryEntity(0, null, "Salary", "Income", R.color.cat_light_blue, R.drawable.baseline_savings_24)
    )
}