package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.AddCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryAddNewViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    // Form state
    val categoryName = MutableLiveData<String>()
    private val _categoryType = MutableLiveData("Expense")
    val categoryType: LiveData<String> = _categoryType

    // Selection state
    private val _selectedColor = MutableLiveData<ColorItem>()
    val selectedColor: LiveData<ColorItem> = _selectedColor

    private val _selectedIcon = MutableLiveData<IconItem>()
    val selectedIcon: LiveData<IconItem> = _selectedIcon

    // Event state
    private val _eventCategoryCreated = MutableLiveData<Boolean>()
    val eventCategoryCreated: LiveData<Boolean> = _eventCategoryCreated

    // Available colors
    private val _colors = MutableStateFlow<List<ColorItem>>(listOf(
        ColorItem(R.color.cat_dark_pink, "Pink"),
        ColorItem(R.color.cat_yellow, "Yellow"),
        ColorItem(R.color.cat_gold, "Gold"),
        ColorItem(R.color.cat_dark_purple, "Purple"),
        ColorItem(R.color.cat_light_green, "Green"),
        ColorItem(R.color.cat_light_purple, "Light Purple"),
        ColorItem(R.color.cat_dark_blue, "Dark Blue"),
        ColorItem(R.color.cat_light_blue, "Light Blue")
    ))
    val colors: StateFlow<List<ColorItem>> = _colors

    // Available icons
    private val _icons = MutableStateFlow<List<IconItem>>(listOf(
        IconItem(R.drawable.baseline_fastfood_24, "Food"),
        IconItem(R.drawable.baseline_local_gas_station_24, "Transport"),
        IconItem(R.drawable.ic_add_alert_24, "Alert"),
        IconItem(R.drawable.baseline_palette_24, "Beauty"),
        IconItem(R.drawable.baseline_savings_24, "Savings"),
        IconItem(R.drawable.baseline_school_24, "Education"),
        IconItem(R.drawable.baseline_theater_comedy_24, "Entertainment"),
        IconItem(R.drawable.baseline_escalator_warning_24, "Family"),
        IconItem(R.drawable.baseline_shopping_bag_24, "Shopping")
    ))
    val icons: StateFlow<List<IconItem>> = _icons

    fun setSelectedColor(color: ColorItem) {
        _selectedColor.value = color
    }

    fun setSelectedIcon(icon: IconItem) {
        _selectedIcon.value = icon
    }

    fun setCategoryType(type: String) {
        _categoryType.value = type
    }

    fun createCategory() {
        val name = categoryName.value
        val color = selectedColor.value
        val icon = selectedIcon.value

        if (name.isNullOrBlank() || color == null || icon == null) {
            _eventCategoryCreated.value = false
            return
        }

        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            val tempUser = User(
                id = userId,
                email = "",
                firstName = "",
                lastName = ""
            )

            val newCategory = Category.new(
                user = tempUser,
                name = name,
                type = _categoryType.value?.lowercase() ?: "expense",
                icon = icon.iconResourceId,
                color = color.colorResourceId
            )
            
            addCategoryUseCase.execute(newCategory)
            _eventCategoryCreated.value = true

        }
    }
}

data class ColorItem(
    val colorResourceId: Int,
    val name: String
)

data class IconItem(
    val iconResourceId: Int,
    val name: String
)
