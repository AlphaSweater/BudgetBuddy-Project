package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.util.Log
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.AddCategoryUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoryUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class CategoryAddNewViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getCategoryUseCase: GetCategoryUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    sealed class LoadingUiState {
        object Idle : LoadingUiState()
        object Loading : LoadingUiState()
        object Loaded : LoadingUiState()
        data class Error(val message: String) : LoadingUiState()
    }

    sealed class SavingUiState {
        object Idle : SavingUiState()
        object Saving : SavingUiState()
        object Success : SavingUiState()
        data class Error(val message: String) : SavingUiState()
    }

    enum class ScreenMode : Serializable {
        EDIT, CREATE
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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // UI State
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _savingUiState = MutableStateFlow<SavingUiState>(SavingUiState.Idle)
    val savingUiState: StateFlow<SavingUiState> = _savingUiState

    private val _loadingUiState = MutableStateFlow<LoadingUiState>(LoadingUiState.Idle)
    val loadingUiState: StateFlow<LoadingUiState> = _loadingUiState

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
        _categoryType.value = category.type.capitalize(java.util.Locale.ROOT)
        _selectedColor.value = _colors.value.find { it.colorResourceId == category.color }
        _selectedIcon.value = _icons.value.find { it.iconResourceId == category.icon }
    }

    private fun loadCategory(id: String) {
        viewModelScope.launch {
            _loadingUiState.value = LoadingUiState.Loading

            val currentUserId = getUserIdUseCase.execute()
            if (currentUserId.isEmpty()) {
                _loadingUiState.value = LoadingUiState.Error("User ID is empty")
                return@launch
            }

            Log.d("CategoryAddNewViewModel", "Loading Category: $id")

            val result = getCategoryUseCase.execute(currentUserId, id)
            when (result) {
                is GetCategoryUseCase.GetCategoryResult.Success -> {
                    Log.d("CategoryAddNewViewModel", "Category loaded successfully: ${result.category.id}")
                    setCategory(result.category)
                    _loadingUiState.value = LoadingUiState.Loaded
                }
                is GetCategoryUseCase.GetCategoryResult.Error -> {
                    Log.e("CategoryAddNewViewModel", "Error loading category: ${result.message}")
                    _loadingUiState.value = LoadingUiState.Error(result.message)
                }
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Form Fields
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _categoryName = MutableStateFlow("")
    val categoryName: StateFlow<String> = _categoryName

    fun setCategoryName(name: String) {
        _categoryName.value = name
        validateForm()
        checkForChanges()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _categoryType = MutableStateFlow("Expense")
    val categoryType: StateFlow<String> = _categoryType

    fun setCategoryType(type: String) {
        _categoryType.value = type
        validateForm()
        checkForChanges()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _selectedColor = MutableStateFlow<CategoryItem.ColorItem?>(null)
    val selectedColor: StateFlow<CategoryItem.ColorItem?> = _selectedColor

    fun setSelectedColor(color: CategoryItem.ColorItem) {
        _selectedColor.value = color
        validateForm()
        checkForChanges()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _selectedIcon = MutableStateFlow<CategoryItem.IconItem?>(null)
    val selectedIcon: StateFlow<CategoryItem.IconItem?> = _selectedIcon

    fun setSelectedIcon(icon: CategoryItem.IconItem) {
        _selectedIcon.value = icon
        validateForm()
        checkForChanges()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Validation
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Validation State
    private val _validationState = MutableStateFlow(ValidationState())
    val validationState: StateFlow<ValidationState> = _validationState

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Validate the form fields and update validation state
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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Validation Actions
    fun showValidationErrors() {
        _validationState.value = _validationState.value.copy(shouldShowErrors = true)
        validateForm()
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
        CategoryItem.IconItem(R.drawable.ic_cat_income, "Income"),
        CategoryItem.IconItem(R.drawable.ic_cat_activity, "Activity"),
        CategoryItem.IconItem(R.drawable.ic_cat_apple, "Apple"),
        CategoryItem.IconItem(R.drawable.ic_cat_bank, "Bank"),
        CategoryItem.IconItem(R.drawable.ic_cat_basket, "Basket"),
        CategoryItem.IconItem(R.drawable.ic_cat_bed, "Bed"),
        CategoryItem.IconItem(R.drawable.ic_cat_book, "Book"),
        CategoryItem.IconItem(R.drawable.ic_cat_box, "Box"),
        CategoryItem.IconItem(R.drawable.ic_cat_brush_cleaning, "Brush Cleaning"),
        CategoryItem.IconItem(R.drawable.ic_cat_cable, "Cable"),
        CategoryItem.IconItem(R.drawable.ic_cat_candy, "Candy"),
        CategoryItem.IconItem(R.drawable.ic_cat_car, "Car"),
        CategoryItem.IconItem(R.drawable.ic_cat_card, "Card"),
        CategoryItem.IconItem(R.drawable.ic_cat_cash, "Cash"),
        CategoryItem.IconItem(R.drawable.ic_cat_chair, "Chair"),
        CategoryItem.IconItem(R.drawable.ic_cat_chart_line, "Chart Line"),
        CategoryItem.IconItem(R.drawable.ic_cat_chart_pie, "Chart Pie"),
        CategoryItem.IconItem(R.drawable.ic_cat_child, "Child"),
        CategoryItem.IconItem(R.drawable.ic_cat_clock, "Clock"),
        CategoryItem.IconItem(R.drawable.ic_cat_cone, "Cone"),
        CategoryItem.IconItem(R.drawable.ic_cat_diamond, "Diamond"),
        CategoryItem.IconItem(R.drawable.ic_cat_document, "Document"),
        CategoryItem.IconItem(R.drawable.ic_cat_drill, "Drill"),
        CategoryItem.IconItem(R.drawable.ic_cat_drink, "Drink"),
        CategoryItem.IconItem(R.drawable.ic_cat_drink_coffee, "Drink Coffee"),
        CategoryItem.IconItem(R.drawable.ic_cat_drink_milk, "Drink Milk"),
        CategoryItem.IconItem(R.drawable.ic_cat_drink_wine, "Drink Wine"),
        CategoryItem.IconItem(R.drawable.ic_cat_education, "Education"),
        CategoryItem.IconItem(R.drawable.ic_cat_education_school, "Education School"),
        CategoryItem.IconItem(R.drawable.ic_cat_education_university, "Education University"),
        CategoryItem.IconItem(R.drawable.ic_cat_electronics, "Electronics"),
        CategoryItem.IconItem(R.drawable.ic_cat_entertainment, "Entertainment"),
        CategoryItem.IconItem(R.drawable.ic_cat_factory, "Factory"),
        CategoryItem.IconItem(R.drawable.ic_cat_fence, "Fence"),
        CategoryItem.IconItem(R.drawable.ic_cat_flower, "Flower"),
        CategoryItem.IconItem(R.drawable.ic_cat_food, "Food"),
        CategoryItem.IconItem(R.drawable.ic_cat_food_burger, "Food Burger"),
        CategoryItem.IconItem(R.drawable.ic_cat_food_cake, "Food Cake"),
        CategoryItem.IconItem(R.drawable.ic_cat_food_cake_slice, "Food Cake Slice"),
        CategoryItem.IconItem(R.drawable.ic_cat_food_chef_hat, "Food Chef Hat"),
        CategoryItem.IconItem(R.drawable.ic_cat_food_cookie, "Food Cookie"),
        CategoryItem.IconItem(R.drawable.ic_cat_food_fish, "Food Fish"),
        CategoryItem.IconItem(R.drawable.ic_cat_food_ham, "Food Ham"),
        CategoryItem.IconItem(R.drawable.ic_cat_food_ice_cream, "Food Ice Cream"),
        CategoryItem.IconItem(R.drawable.ic_cat_food_pizza, "Food Pizza"),
        CategoryItem.IconItem(R.drawable.ic_cat_food_popcorn, "Food Popcorn"),
        CategoryItem.IconItem(R.drawable.ic_cat_food_utensils, "Food Utensils"),
        CategoryItem.IconItem(R.drawable.ic_cat_fuel, "Fuel"),
        CategoryItem.IconItem(R.drawable.ic_cat_game, "Game"),
        CategoryItem.IconItem(R.drawable.ic_cat_gift, "Gift"),
        CategoryItem.IconItem(R.drawable.ic_cat_globe, "Globe"),
        CategoryItem.IconItem(R.drawable.ic_cat_gym, "Gym"),
        CategoryItem.IconItem(R.drawable.ic_cat_headset, "Headset"),
        CategoryItem.IconItem(R.drawable.ic_cat_health_care, "Health Care"),
        CategoryItem.IconItem(R.drawable.ic_cat_hospital, "Hospital"),
        CategoryItem.IconItem(R.drawable.ic_cat_hotel, "Hotel"),
        CategoryItem.IconItem(R.drawable.ic_cat_house, "House"),
        CategoryItem.IconItem(R.drawable.ic_cat_income, "Income"),
        CategoryItem.IconItem(R.drawable.ic_cat_laptop, "Laptop"),
        CategoryItem.IconItem(R.drawable.ic_cat_lightbulb, "Lightbulb"),
        CategoryItem.IconItem(R.drawable.ic_cat_lock, "Lock"),
        CategoryItem.IconItem(R.drawable.ic_cat_medal, "Medal"),
        CategoryItem.IconItem(R.drawable.ic_cat_medical, "Medical"),
        CategoryItem.IconItem(R.drawable.ic_cat_medicine_pill, "Medicine Pill"),
        CategoryItem.IconItem(R.drawable.ic_cat_medicine_syringe, "Medicine Syringe"),
        CategoryItem.IconItem(R.drawable.ic_cat_message, "Message"),
        CategoryItem.IconItem(R.drawable.ic_cat_monitor, "Monitor"),
        CategoryItem.IconItem(R.drawable.ic_cat_monitor_smartphone, "Monitor Smartphone"),
        CategoryItem.IconItem(R.drawable.ic_cat_moon, "Moon"),
        CategoryItem.IconItem(R.drawable.ic_cat_movie, "Movie"),
        CategoryItem.IconItem(R.drawable.ic_cat_music, "Music"),
        CategoryItem.IconItem(R.drawable.ic_cat_music_guitar, "Music Guitar"),
        CategoryItem.IconItem(R.drawable.ic_cat_music_speaker, "Music Speaker"),
        CategoryItem.IconItem(R.drawable.ic_cat_notebook, "Notebook"),
        CategoryItem.IconItem(R.drawable.ic_cat_paint, "Paint"),
        CategoryItem.IconItem(R.drawable.ic_cat_people, "People"),
        CategoryItem.IconItem(R.drawable.ic_cat_person, "Person"),
        CategoryItem.IconItem(R.drawable.ic_cat_pet, "Pet"),
        CategoryItem.IconItem(R.drawable.ic_cat_phone, "Phone"),
        CategoryItem.IconItem(R.drawable.ic_cat_plane, "Plane"),
        CategoryItem.IconItem(R.drawable.ic_cat_rocket, "Rocket"),
        CategoryItem.IconItem(R.drawable.ic_cat_roller_coaster, "Roller Coaster"),
        CategoryItem.IconItem(R.drawable.ic_cat_sailboat, "Sailboat"),
        CategoryItem.IconItem(R.drawable.ic_cat_scale, "Scale"),
        CategoryItem.IconItem(R.drawable.ic_cat_shield, "Shield"),
        CategoryItem.IconItem(R.drawable.ic_cat_shop, "Shop"),
        CategoryItem.IconItem(R.drawable.ic_cat_shopping_bag, "Shopping Bag"),
        CategoryItem.IconItem(R.drawable.ic_cat_shopping_cart, "Shopping Cart"),
        CategoryItem.IconItem(R.drawable.ic_cat_shrimp, "Shrimp"),
        CategoryItem.IconItem(R.drawable.ic_cat_signal, "Signal"),
        CategoryItem.IconItem(R.drawable.ic_cat_smartphone, "Smartphone"),
        CategoryItem.IconItem(R.drawable.ic_cat_sport_ball, "Sport Ball"),
        CategoryItem.IconItem(R.drawable.ic_cat_sport_bike, "Sport Bike"),
        CategoryItem.IconItem(R.drawable.ic_cat_sport_volleyball, "Sport Volleyball"),
        CategoryItem.IconItem(R.drawable.ic_cat_star, "Star"),
        CategoryItem.IconItem(R.drawable.ic_cat_stationary_pencil, "Stationary Pencil"),
        CategoryItem.IconItem(R.drawable.ic_cat_stationary_pencil_ruler, "Stationary Pencil Ruler"),
        CategoryItem.IconItem(R.drawable.ic_cat_sticky_note, "Sticky Note"),
        CategoryItem.IconItem(R.drawable.ic_cat_target, "Target"),
        CategoryItem.IconItem(R.drawable.ic_cat_travel, "Travel"),
        CategoryItem.IconItem(R.drawable.ic_cat_trophy, "Trophy"),
        CategoryItem.IconItem(R.drawable.ic_cat_vehicle, "Vehicle"),
        CategoryItem.IconItem(R.drawable.ic_cat_warehouse, "Warehouse"),
        CategoryItem.IconItem(R.drawable.ic_cat_waves, "Waves"),
        CategoryItem.IconItem(R.drawable.ic_cat_work, "Work"),
        CategoryItem.IconItem(R.drawable.ic_cat_wrench, "Wrench"),
    ))
    val icons: StateFlow<List<CategoryItem.IconItem>> = _icons

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Initialize ViewModel
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
    // Category Creation/Update
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun createCategory() {
        if (!validateForm()) {
            showValidationErrors()
            return
        }

        viewModelScope.launch {
            _savingUiState.value = SavingUiState.Saving
            
            try {
                val userId = getUserIdUseCase.execute()
                if (userId.isEmpty()) {
                    _savingUiState.value = SavingUiState.Error("User ID is empty")
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

                when (screenMode.value) {
                    ScreenMode.EDIT -> {
                        // Update existing category
                        val originalCategory = _category.value ?: return@launch

                        val updatedCategory = originalCategory.copy(
                            name = newCategory.name,
                            type = newCategory.type,
                            icon = newCategory.icon,
                            color = newCategory.color
                        )

                        updateCategoryUseCase.execute(updatedCategory)
                            .catch { e ->
                                Log.e("CategoryAddNewViewModel", "Error in category update flow: ${e.message}")
                                _savingUiState.value = SavingUiState.Error(e.message ?: "Failed to update category")
                            }
                            .collect { result ->
                                when (result) {
                                    is UpdateCategoryUseCase.UpdateCategoryResult.Success -> {
                                        Log.d("CategoryAddNewViewModel", "Category updated successfully: ${result.categoryId}")
                                        reset()
                                        _savingUiState.value = SavingUiState.Success
                                    }
                                    is UpdateCategoryUseCase.UpdateCategoryResult.Error -> {
                                        Log.e("CategoryAddNewViewModel", "Error updating category: ${result.message}")
                                        _savingUiState.value = SavingUiState.Error(result.message)
                                    }
                                }
                            }
                    }
                    ScreenMode.CREATE -> {
                        // Create new category
                        addCategoryUseCase.execute(newCategory)
                            .catch { e ->
                                Log.e("CategoryAddNewViewModel", "Error in category creation flow: ${e.message}")
                                _savingUiState.value = SavingUiState.Error(e.message ?: "Failed to add category")
                            }
                            .collect { result ->
                                when (result) {
                                    is AddCategoryUseCase.AddCategoryResult.Success -> {
                                        Log.d("CategoryAddNewViewModel", "Category added successfully: ${result.categoryId}")
                                        reset()
                                        _savingUiState.value = SavingUiState.Success
                                    }
                                    is AddCategoryUseCase.AddCategoryResult.Error -> {
                                        Log.e("CategoryAddNewViewModel", "Error adding category: ${result.message}")
                                        _savingUiState.value = SavingUiState.Error(result.message)
                                    }
                                }
                            }
                    }
                    else -> {
                        _savingUiState.value = SavingUiState.Error("Invalid screen mode for category operation")
                    }
                }
            } catch (e: Exception) {
                Log.e("CategoryAddNewViewModel", "Exception in category operation: ${e.message}")
                _savingUiState.value = SavingUiState.Error(e.message ?: "Failed to process category")
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
        _savingUiState.value = SavingUiState.Idle
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Saved Changes Check
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
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
        _categoryType.value = originalCategory.type.capitalize(java.util.Locale.ROOT)
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
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\