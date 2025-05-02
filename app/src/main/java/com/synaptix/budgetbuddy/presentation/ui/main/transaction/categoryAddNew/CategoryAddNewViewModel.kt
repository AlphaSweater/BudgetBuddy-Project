package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import androidx.lifecycle.*
import com.synaptix.budgetbuddy.core.model.CategoryIn
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryAddNewViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentUserId: Int = savedStateHandle["currentUserId"] ?: 0

    val categoryName = MutableLiveData<String>()
    val selectedIcon = MutableLiveData<Int>() // Drawable resource ID
    val selectedColor = MutableLiveData<Int>() // Color int value
    val categoryType = MutableLiveData("Expense")

    private val _eventCategoryCreated = MutableLiveData<Boolean>()
    val eventCategoryCreated: LiveData<Boolean> = _eventCategoryCreated

    fun createCategory() {
        val name = categoryName.value
        val icon = selectedIcon.value
        val color = selectedColor.value
        val type = categoryType.value ?: "Expense"

        if (!name.isNullOrBlank() && icon != null && color != null) {
            viewModelScope.launch {
                val userId = getUserIdUseCase.execute()

                val newCategory = CategoryIn(
                    categoryName = name,
                    categoryIcon = icon,
                    categoryColor = color,
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
