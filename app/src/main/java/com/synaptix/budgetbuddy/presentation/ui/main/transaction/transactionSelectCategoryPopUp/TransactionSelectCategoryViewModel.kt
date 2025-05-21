package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectCategoryPopUp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoriesUseCase
import com.synaptix.budgetbuddy.data.firebase.model.CategoryDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the category selection screen.
 * Handles:
 * 1. Loading categories from Firebase
 * 2. Filtering categories based on search query
 * 3. Maintaining the filtered state
 */
@HiltViewModel
class TransactionSelectCategoryViewModel @Inject constructor(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
): ViewModel() {
    // Original list of categories
    private var originalCategories = emptyList<Category>()
    
    // LiveData for filtered categories that the UI observes
    private val _filteredCategories = MutableLiveData<List<Category>>()
    val filteredCategories: LiveData<List<Category>> get() = _filteredCategories

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Loads categories for the current user and updates both original and filtered lists.
     * This should be called when the screen is first created.
     */
    fun loadCategories() {
        viewModelScope.launch {
            try {
                val userId = getUserIdUseCase.execute()
                when (val result = getCategoriesUseCase.execute(userId)) {
                    is GetCategoriesUseCase.GetCategoriesResult.Success -> {
                        originalCategories = result.categories
                        _filteredCategories.value = originalCategories
                    }
                    is GetCategoriesUseCase.GetCategoriesResult.Error -> {
                        _error.value = result.message
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load categories"
            }
        }
    }

    /**
     * Filters the categories based on the provided search query.
     * Performs case-insensitive search on category names.
     * @param query The search query to filter categories
     */
    fun filterCategories(query: String) {
        _filteredCategories.value = if (query.isEmpty()) {
            originalCategories
        } else {
            originalCategories.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }
}