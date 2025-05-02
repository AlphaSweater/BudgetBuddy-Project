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

package com.synaptix.budgetbuddy.data._default

import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.data.entity.CategoryEntity

// Object holding the default categories that will be added to the database
// These categories are global and accessible by all users, as they have a null User ID
object CategoryDefualts {
    // List of default categories with necessary details (name, type, color, icon)
    val defaultCategories = listOf(
        // Category for Food with corresponding icon and color
        CategoryEntity(0, null, "Food", "expense", R.color.cat_dark_pink, R.drawable.baseline_fastfood_24),

        // Category for Transport with corresponding icon and color
        CategoryEntity(0, null, "Transport", "expense", R.color.cat_yellow, R.drawable.baseline_local_gas_station_24),

        // Category for HealthCare with corresponding icon and color
        CategoryEntity(0, null, "HealthCare", "expense", R.color.cat_gold, R.drawable.ic_add_alert_24),

        // Category for Beauty with corresponding icon and color
        CategoryEntity(0, null, "Beauty", "expense", R.color.cat_dark_purple, R.drawable.baseline_palette_24),

        // Category for Bills & Fees with corresponding icon and color
        CategoryEntity(0, null, "Bills & Fees", "expense", R.color.cat_light_green, R.drawable.baseline_savings_24),

        // Category for Education with corresponding icon and color
        CategoryEntity(0, null, "Education", "expense", R.color.cat_yellow, R.drawable.baseline_school_24),

        // Category for Entertainment with corresponding icon and color
        CategoryEntity(0, null, "Entertainment", "expense", R.color.cat_light_purple, R.drawable.baseline_theater_comedy_24),

        // Category for Family & Friends with corresponding icon and color
        CategoryEntity(0, null, "Family & Friends", "expense", R.color.cat_dark_blue, R.drawable.baseline_escalator_warning_24),

        // Category for Groceries with corresponding icon and color
        CategoryEntity(0, null, "Groceries", "expense", R.color.cat_light_blue, R.drawable.baseline_shopping_bag_24),

        // Category for Salary with corresponding icon and color
        CategoryEntity(0, null, "Salary", "Income", R.color.cat_light_blue, R.drawable.baseline_savings_24)
    )
}
