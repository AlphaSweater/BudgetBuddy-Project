package com.synaptix.budgetbuddy.core.usecase.main.category

import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.CategoryColor
import com.synaptix.budgetbuddy.core.model.CategoryIcon
import com.synaptix.budgetbuddy.data.repository.CategoryAssetsRepository
import javax.inject.Inject

class InitializeCategoryAssetsUseCase @Inject constructor(
    private val repository: CategoryAssetsRepository
) {
    suspend fun execute() {
        initializeDefaultColors()
        initializeDefaultIcons()
    }

    private suspend fun initializeDefaultColors() {
        val defaultColors = listOf(
            CategoryColor(name = "Light Pink", colorValue = R.color.cat_light_pink, hexCode = "#FFB6C1"),
            CategoryColor(name = "Dark Pink", colorValue = R.color.cat_dark_pink, hexCode = "#FF69B4"),
            CategoryColor(name = "Light Purple", colorValue = R.color.cat_light_purple, hexCode = "#E6E6FA"),
            CategoryColor(name = "Dark Purple", colorValue = R.color.cat_dark_purple, hexCode = "#9370DB"),
            CategoryColor(name = "Light Blue", colorValue = R.color.cat_light_blue, hexCode = "#87CEEB"),
            CategoryColor(name = "Dark Blue", colorValue = R.color.cat_dark_blue, hexCode = "#4169E1"),
            CategoryColor(name = "Light Green", colorValue = R.color.cat_light_green, hexCode = "#90EE90"),
            CategoryColor(name = "Dark Green", colorValue = R.color.cat_dark_green, hexCode = "#228B22"),
            CategoryColor(name = "Yellow", colorValue = R.color.cat_yellow, hexCode = "#FFD700"),
            CategoryColor(name = "Orange", colorValue = R.color.cat_orange, hexCode = "#FFA500"),
            CategoryColor(name = "Gold", colorValue = R.color.cat_gold, hexCode = "#DAA520")
        )
        repository.initializeDefaultColors(defaultColors)
    }

    private suspend fun initializeDefaultIcons() {
        val defaultIcons = listOf(
            CategoryIcon(name = "Birthday", iconResourceId = R.drawable.baseline_cake_24),
            CategoryIcon(name = "Baby", iconResourceId = R.drawable.baseline_child_friendly_24),
            CategoryIcon(name = "Travel", iconResourceId = R.drawable.baseline_airplanemode_active_24),
            CategoryIcon(name = "Food", iconResourceId = R.drawable.baseline_fastfood_24),
            CategoryIcon(name = "Utilities", iconResourceId = R.drawable.baseline_lightbulb_24),
            CategoryIcon(name = "Gas", iconResourceId = R.drawable.baseline_local_gas_station_24),
            CategoryIcon(name = "Shopping", iconResourceId = R.drawable.baseline_shopping_bag_24),
            CategoryIcon(name = "Technology", iconResourceId = R.drawable.baseline_computer_24),
            CategoryIcon(name = "Entertainment", iconResourceId = R.drawable.baseline_theater_comedy_24)
        )
        repository.initializeDefaultIcons(defaultIcons)
    }
} 