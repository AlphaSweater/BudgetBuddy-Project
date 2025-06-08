package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.util.Log
import androidx.lifecycle.SavedStateHandle
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
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class CategoryAddNewViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    enum class ScreenMode : Serializable {
        EDIT, CREATE
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Screen Mode
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _screenMode = MutableStateFlow(
        savedStateHandle["screenMode"] ?: ScreenMode.CREATE
    )
    val screenMode: StateFlow<ScreenMode> get() = _screenMode

    fun setScreenMode(mode: ScreenMode) {
        _screenMode.value = mode
        savedStateHandle["screenMode"] = mode
    }

    private val _screenModeBusy = MutableStateFlow(false)
    val screenModeBusy: StateFlow<Boolean> get() = _screenModeBusy

    fun setScreenModeBusy(isBusy: Boolean) {
        _screenModeBusy.value = isBusy
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Category Data
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val categoryId: String? = savedStateHandle["categoryId"]

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category

    fun setCategory(category: Category?) {
        _category.value = category
        category?.let { populateCategoryData(it) }
    }

    private fun populateCategoryData(category: Category) {
        _categoryName.value = category.name
        _categoryType.value = category.type.capitalize()
        _selectedColor.value = _colors.value.find { it.colorResourceId == category.color }
        _selectedIcon.value = _icons.value.find { it.iconResourceId == category.icon }
    }

    private fun loadCategory(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val currentUserId = getUserIdUseCase.execute()
            if (currentUserId.isEmpty()) {
                _uiState.value = UiState.Error("User ID is empty")
                return@launch
            }

            Log.d("CategoryAddNewViewModel", "Loading Category: $id")

            // TODO: Add GetCategoryUseCase and implement loading
            // For now, we'll just show an error
            _uiState.value = UiState.Error("Category loading not implemented yet")
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // UI State
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Form State
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _categoryName = MutableStateFlow("")
    val categoryName: StateFlow<String> = _categoryName

    private val _categoryType = MutableStateFlow("Expense")
    val categoryType: StateFlow<String> = _categoryType

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Selection State
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _selectedColor = MutableStateFlow<CategoryItem.ColorItem?>(null)
    val selectedColor: StateFlow<CategoryItem.ColorItem?> = _selectedColor

    private val _selectedIcon = MutableStateFlow<CategoryItem.IconItem?>(null)
    val selectedIcon: StateFlow<CategoryItem.IconItem?> = _selectedIcon

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Available Options
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _colors = MutableStateFlow<List<CategoryItem.ColorItem>>(listOf(
        CategoryItem.ColorItem(R.color.cat_dark_pink, "Pink"),
        CategoryItem.ColorItem(R.color.cat_yellow, "Yellow"),
        CategoryItem.ColorItem(R.color.cat_gold, "Gold"),
        CategoryItem.ColorItem(R.color.cat_dark_purple, "Purple"),
        CategoryItem.ColorItem(R.color.cat_light_green, "Green"),
        CategoryItem.ColorItem(R.color.cat_light_purple, "Light Purple"),
        CategoryItem.ColorItem(R.color.cat_dark_blue, "Dark Blue"),
        CategoryItem.ColorItem(R.color.cat_light_blue, "Light Blue")
    ))
    val colors: StateFlow<List<CategoryItem.ColorItem>> = _colors

    private val _icons = MutableStateFlow<List<CategoryItem.IconItem>>(listOf(
        CategoryItem.IconItem(R.drawable.ic_cat_food, "Food"),
        CategoryItem.IconItem(R.drawable.baseline_local_gas_station_24, "Transport"),
        CategoryItem.IconItem(R.drawable.ic_ui_notification, "Alert"),
        CategoryItem.IconItem(R.drawable.ic_cat_art, "Beauty"),
        CategoryItem.IconItem(R.drawable.ic_ui_budget, "Savings"),
        CategoryItem.IconItem(R.drawable.ic_cat_education, "Education"),
        CategoryItem.IconItem(R.drawable.baseline_theater_comedy_24, "Entertainment"),
        CategoryItem.IconItem(R.drawable.baseline_escalator_warning_24, "Family"),
        CategoryItem.IconItem(R.drawable.baseline_shopping_bag_24, "Shopping"),
        CategoryItem.IconItem(R.drawable.ic_cat_pet, "Pets"),
        CategoryItem.IconItem(R.drawable.ic_cat_electronics, "Electronics"),
        CategoryItem.IconItem(R.drawable.ic_cat_medical, "Mecical"),
        CategoryItem.IconItem(R.drawable.ic_cat_vehicle, "Vehicle"),
        CategoryItem.IconItem(R.drawable.ic_cat_income, "Income")
    ))
    val icons: StateFlow<List<CategoryItem.IconItem>> = _icons

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Form Actions
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun setCategoryName(name: String) {
        _categoryName.value = name
        validateForm()
        checkForChanges()
    }

    fun setSelectedColor(color: CategoryItem.ColorItem) {
        _selectedColor.value = color
        validateForm()
        checkForChanges()
    }

    fun setSelectedIcon(icon: CategoryItem.IconItem) {
        _selectedIcon.value = icon
        validateForm()
        checkForChanges()
    }

    fun setCategoryType(type: String) {
        _categoryType.value = type
        validateForm()
        checkForChanges()
    }

    fun showValidationErrors() {
        _validationState.value = _validationState.value.copy(shouldShowErrors = true)
        validateForm()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Validation
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
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

    private fun validateColor(color: CategoryItem.ColorItem?): Pair<Boolean, String?> {
        val isValid = color != null
        val error = if (isValid) null else "Please select a color"
        return Pair(isValid, error)
    }

    private fun validateIcon(icon: CategoryItem.IconItem?): Pair<Boolean, String?> {
        val isValid = icon != null
        val error = if (isValid) null else "Please select an icon"
        return Pair(isValid, error)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Initialize
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    init {
        when (screenMode.value) {
            ScreenMode.EDIT -> {
                categoryId?.let { loadCategory(it) }
            }
            ScreenMode.CREATE -> {
                reset()
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Category Creation
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
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

                // If we're in edit mode, we need to update the existing category
                if (screenMode.value == ScreenMode.EDIT && categoryId != null) {
                    // TODO: Add UpdateCategoryUseCase and implement updating
                    _uiState.value = UiState.Error("Category updating not implemented yet")
                    return@launch
                }

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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Reset
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun reset() {
        _categoryName.value = ""
        _categoryType.value = "Expense"
        _selectedColor.value = null
        _selectedIcon.value = null
        _validationState.value = ValidationState(shouldShowErrors = false)
        _uiState.value = UiState.Initial
    }

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges

    private fun checkForChanges() {
        val originalCategory = _category.value
        if (originalCategory == null) {
            _hasUnsavedChanges.value = false
            return
        }

        val hasChanges = originalCategory.name != _categoryName.value ||
                originalCategory.type != _categoryType.value.lowercase() ||
                originalCategory.icon != (_selectedIcon.value?.iconResourceId ?: R.drawable.ic_circle_24) ||
                originalCategory.color != (_selectedColor.value?.colorResourceId ?: R.color.cat_dark_pink)

        _hasUnsavedChanges.value = hasChanges
    }

    fun revertChanges() {
        val originalCategory = _category.value ?: return

        // Restore all values from the original category
        _categoryName.value = originalCategory.name
        _categoryType.value = originalCategory.type.capitalize()
        _selectedColor.value = _colors.value.find { it.colorResourceId == originalCategory.color }
        _selectedIcon.value = _icons.value.find { it.iconResourceId == originalCategory.icon }

        // Reset validation state
        _validationState.value = ValidationState(shouldShowErrors = false)

        // Reset unsaved changes flag
        _hasUnsavedChanges.value = false
    }
}

sealed class CategoryItem {
    data class ColorItem(
        val colorResourceId: Int,
        val name: String
    ) : CategoryItem()

    data class IconItem(
        val iconResourceId: Int,
        val name: String
    ) : CategoryItem()
}