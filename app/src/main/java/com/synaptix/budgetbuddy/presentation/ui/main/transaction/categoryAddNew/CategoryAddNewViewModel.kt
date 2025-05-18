package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import androidx.lifecycle.*
import com.synaptix.budgetbuddy.core.model.CategoryColor
import com.synaptix.budgetbuddy.core.model.CategoryIcon
import com.synaptix.budgetbuddy.core.model.CategoryIn
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoryColorsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoryIconsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryAddNewViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getCategoryColorsUseCase: GetCategoryColorsUseCase,
    private val getCategoryIconsUseCase: GetCategoryIconsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentUserId: Int = savedStateHandle["currentUserId"] ?: 0

    val categoryName = MutableLiveData<String>()
    private val _selectedColor = MutableLiveData<CategoryColor>()
    val selectedColor: LiveData<CategoryColor> = _selectedColor
    private val _selectedIcon = MutableLiveData<CategoryIcon>()
    val selectedIcon: LiveData<CategoryIcon> = _selectedIcon
    val categoryType = MutableLiveData("Expense")

    private val _colors = MutableStateFlow<List<CategoryColor>>(emptyList())
    val colors: StateFlow<List<CategoryColor>> = _colors

    private val _icons = MutableStateFlow<List<CategoryIcon>>(emptyList())
    val icons: StateFlow<List<CategoryIcon>> = _icons

    private val _eventCategoryCreated = MutableLiveData<Boolean>()
    val eventCategoryCreated: LiveData<Boolean> = _eventCategoryCreated

    init {
        loadColorsAndIcons()
    }

    private fun loadColorsAndIcons() {
        viewModelScope.launch {
            _colors.value = getCategoryColorsUseCase.execute()
            _icons.value = getCategoryIconsUseCase.execute()
        }
    }

    fun setSelectedColor(color: CategoryColor) {
        _selectedColor.value = color
    }

    fun setSelectedIcon(icon: CategoryIcon) {
        _selectedIcon.value = icon
    }

    fun createCategory() {
        val name = categoryName.value
        val icon = _selectedIcon.value
        val color = _selectedColor.value
        val type = categoryType.value ?: "Expense"

        if (!name.isNullOrBlank() && icon != null && color != null) {
            viewModelScope.launch {
                val userId = getUserIdUseCase.execute()

                val newCategory = CategoryIn(
                    categoryName = name,
                    categoryIcon = icon.iconResourceId,
                    categoryColor = color.colorValue,
                    categoryType = type,
                    userId = userId
                )

                addCategoryUseCase.execute(newCategory)
                _eventCategoryCreated.value = true
            }
        } else {
            _eventCategoryCreated.value = false
        }
    }
}
