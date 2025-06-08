package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.util.Log
import androidx.compose.material3.Icon
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.AddCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryAddNewViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    // UI State
    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    data class ValidationState(
        val isNameValid: Boolean = false,
        val isTypeValid: Boolean = false,
        val isColorValid: Boolean = false,
        val isIconValid: Boolean = false,
        val nameError: String? = null,
        val typeError: String? = null,
        val colorError: String? = null,
        val iconError: String? = null,
        val shouldShowErrors: Boolean = false
    )

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _validationState = MutableStateFlow(ValidationState())
    val validationState: StateFlow<ValidationState> = _validationState

    // Form state
    private val _categoryName = MutableStateFlow("")
    val categoryName: StateFlow<String> = _categoryName

    private val _categoryType = MutableStateFlow("Expense")
    val categoryType: StateFlow<String> = _categoryType

    // Selection state
    private val _selectedColor = MutableStateFlow<ColorItem?>(null)
    val selectedColor: StateFlow<ColorItem?> = _selectedColor

    private val _selectedIcon = MutableStateFlow<IconItem?>(null)
    val selectedIcon: StateFlow<IconItem?> = _selectedIcon

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
        IconItem(R.drawable.ic_cat_food, "Food"),
        IconItem(R.drawable.baseline_local_gas_station_24, "Transport"),
        IconItem(R.drawable.ic_ui_notification, "Alert"),
        IconItem(R.drawable.ic_cat_art, "Beauty"),
        IconItem(R.drawable.ic_ui_budget, "Savings"),
        IconItem(R.drawable.ic_cat_education, "Education"),
        IconItem(R.drawable.baseline_theater_comedy_24, "Entertainment"),
        IconItem(R.drawable.baseline_escalator_warning_24, "Family"),
        IconItem(R.drawable.baseline_shopping_bag_24, "Shopping"),
        IconItem(R.drawable.ic_cat_pet, "Pets"),
        IconItem(R.drawable.ic_cat_electronics, "Electronics"),
        IconItem(R.drawable.ic_cat_medical, "Mecical"),
        IconItem(R.drawable.ic_cat_vehicle, "Vehicle"),
        IconItem(R.drawable.ic_cat_income, "Income"),
    ))
    val icons: StateFlow<List<IconItem>> = _icons

    fun setCategoryName(name: String) {
        _categoryName.value = name
        validateForm()
    }

    fun setSelectedColor(color: ColorItem) {
        _selectedColor.value = color
        validateForm()
    }

    fun setSelectedIcon(icon: IconItem) {
        _selectedIcon.value = icon
        validateForm()
    }

    fun setCategoryType(type: String) {
        _categoryType.value = type
        validateForm()
    }

    fun showValidationErrors() {
        _validationState.value = _validationState.value.copy(shouldShowErrors = true)
        validateForm()
    }

    fun validateForm(): Boolean {
        val name = _categoryName.value
        val type = _categoryType.value
        val color = _selectedColor.value
        val icon = _selectedIcon.value

        val (isNameValid, nameError) = validateName(name)
        val (isTypeValid, typeError) = validateType(type)
        val (isColorValid, colorError) = validateColor(color)
        val (isIconValid, iconError) = validateIcon(icon)

        _validationState.value = _validationState.value.copy(
            isNameValid = isNameValid,
            isTypeValid = isTypeValid,
            isColorValid = isColorValid,
            isIconValid = isIconValid,
            nameError = nameError,
            typeError = typeError,
            colorError = colorError,
            iconError = iconError
        )

        return isNameValid && isTypeValid && isColorValid && isIconValid
    }

    private fun validateName(name: String): Pair<Boolean, String?> {
        val isValid = name.isNotEmpty()
        val error = if (isValid) null else "Category name cannot be empty"
        return Pair(isValid, error)
    }

    private fun validateType(type: String): Pair<Boolean, String?> {
        val isValid = type.isNotEmpty()
        val error = if (isValid) null else "Category type must be selected"
        return Pair(isValid, error)
    }

    private fun validateColor(color: ColorItem?): Pair<Boolean, String?> {
        val isValid = color != null
        val error = if (isValid) null else "Please select a color"
        return Pair(isValid, error)
    }

    private fun validateIcon(icon: IconItem?): Pair<Boolean, String?> {
        val isValid = icon != null
        val error = if (isValid) null else "Please select an icon"
        return Pair(isValid, error)
    }

    fun createCategory() {
        if (!validateForm()) {
            showValidationErrors()
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val userId = getUserIdUseCase.execute()
                if (userId.isEmpty()) {
                    _uiState.value = UiState.Error("User ID is empty")
                    return@launch
                }

                val tempUser = User(
                    id = userId,
                    email = "",
                    firstName = "",
                    lastName = ""
                )

                val newCategory = Category.new(
                    user = tempUser,
                    name = _categoryName.value,
                    type = _categoryType.value.lowercase(),
                    icon = _selectedIcon.value?.iconResourceId ?: R.drawable.ic_circle_24,
                    color = _selectedColor.value?.colorResourceId ?: R.color.cat_dark_pink
                )

                addCategoryUseCase.execute(newCategory)
                    .catch { e ->
                        Log.e("CategoryAddNewViewModel", "Error in category creation flow: ${e.message}")
                        _uiState.value = UiState.Error(e.message ?: "Failed to add category")
                    }
                    .collect { result ->
                        when (result) {
                            is AddCategoryUseCase.AddCategoryResult.Success -> {
                                Log.d("CategoryAddNewViewModel", "Category added successfully: ${result.categoryId}")
                                _uiState.value = UiState.Success
                            }
                            is AddCategoryUseCase.AddCategoryResult.Error -> {
                                Log.e("CategoryAddNewViewModel", "Error adding category: ${result.message}")
                                _uiState.value = UiState.Error(result.message)
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("CategoryAddNewViewModel", "Exception adding category: ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Failed to add category")
            }
        }
    }

    fun reset() {
        _categoryName.value = ""
        _categoryType.value = "Expense"
        _selectedColor.value = null
        _selectedIcon.value = null
        _validationState.value = ValidationState(shouldShowErrors = false)
        _uiState.value = UiState.Initial
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
