package com.synaptix.budgetbuddy.data._default

import com.synaptix.budgetbuddy.data.entity.CategoryEntity

//object list of default categories for the database
//null User ID makes default categories global and accessible by all users
object CategoryDefualts {
    val defaultCategories = listOf(
        CategoryEntity(0, null, "Food", "@color/cat_dark_pink", "@drawable/baseline_fastfood_24", "expense"),
        CategoryEntity(0, null, "Transport", "@color/cat_yellow", "@drawable/baseline_local_gas_station_24", "expense"),
        CategoryEntity(0, null, "HealthCare", "@color/cat_gold", "@drawable/ic_add_alert_24", "expense"),
        CategoryEntity(0, null, "Beauty", "@color/cat_dark_purple", "@drawable/baseline_palette_24", "expense"),
        CategoryEntity(0, null, "Bills & Fees", "@color/cat_light_green", "@drawable/baseline_savings_24", "expense"),
        CategoryEntity(0, null, "Education", "@color/cat_yellow", "@drawable/baseline_school_24", "expense"),
        CategoryEntity(0, null, "Entertainment", "@color/cat_light_purple", "@drawable/baseline_theater_comedy_24", "expense"),
        CategoryEntity(0, null, "Family & Friends", "@color/cat_dark_blue", "@drawable/baseline_escalator_warning_24", "expense"),
        CategoryEntity(0, null, "Groceries", "@color/cat_light_blue", "@drawable/baseline_shopping_bag_24", "expense"),
        CategoryEntity(0, null, "Salary", "@color/cat_light_blue", "@drawable/baseline_savings_24", "Income")
    )
}