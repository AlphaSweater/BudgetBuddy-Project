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

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.budgetSelectCategoryPopUp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel class to handle the logic for loading budget categories
@HiltViewModel
class BudgetSelectCategoryViewModel @Inject constructor(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val categories: List<Category>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filteredCategories = MutableStateFlow<List<Category>>(emptyList())
    val filteredCategories: StateFlow<List<Category>> = _filteredCategories

    private val _selectedCategories = MutableStateFlow<List<Category>>(emptyList())
    val selectedCategories: StateFlow<List<Category>> = _selectedCategories

    init {
        viewModelScope.launch {
            combine(_searchQuery, _uiState) { query, state ->
                when (state) {
                    is UiState.Success -> {
                        val categories = state.categories
                        if (query.isEmpty()) {
                            categories
                        } else {
                            categories.filter {
                                it.name.contains(query, ignoreCase = true)
                            }
                        }
                    }
                    else -> emptyList()
                }
            }.collect { filtered ->
                _filteredCategories.value = filtered
            }
        }
    }

    /**
     * Loads categories for the current user and updates both original and filtered lists.
     * This should be called when the screen is first created.
     * @param initialSelectedCategories Optional list of categories that should be pre-selected
     */
    fun loadCategories(initialSelectedCategories: List<Category> = emptyList()) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _selectedCategories.value = initialSelectedCategories

            try {
                val userId = getUserIdUseCase.execute()
                if (userId.isEmpty()) {
                    _uiState.value = UiState.Error("User ID is empty")
                    return@launch
                }

                getCategoriesUseCase.execute(userId)
                    .catch { e ->
                        Log.e("BudgetSelectCategoryViewModel", "Error in categories flow: ${e.message}")
                        _uiState.value = UiState.Error(e.message ?: "Failed to load categories")
                    }
                    .collect { result ->
                        when (result) {
                            is GetCategoriesUseCase.GetCategoriesResult.Success -> {
                                Log.d("BudgetSelectCategoryViewModel", "Categories loaded successfully: ${result.categories.size}")
                                // Preserve selection state when updating categories
                                val selectedIds = _selectedCategories.value.map { it.id }.toSet()
                                val categoriesWithSelection = result.categories.map { category ->
                                    category.copy(isSelected = selectedIds.contains(category.id))
                                }
                                _uiState.value = UiState.Success(categoriesWithSelection)
                            }
                            is GetCategoriesUseCase.GetCategoriesResult.Error -> {
                                Log.e("BudgetSelectCategoryViewModel", "Error loading categories: ${result.message}")
                                _uiState.value = UiState.Error(result.message)
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("BudgetSelectCategoryViewModel", "Exception loading categories: ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Failed to load categories")
            }
        }
    }

    /**
     * Filters the categories based on the provided search query.
     * Performs case-insensitive search on category names.
     * @param query The search query to filter categories
     */
    fun filterCategories(query: String) {
        _searchQuery.value = query
    }

    /**
     * Updates the selected categories and preserves the selection state
     * @param selectedCategories The new list of selected categories
     */
    fun updateSelectedCategories(selectedCategories: List<Category>) {
        _selectedCategories.value = selectedCategories
        // Update the selection state in the current UI state if it's a Success state
        if (_uiState.value is UiState.Success) {
            val currentCategories = (_uiState.value as UiState.Success).categories
            val selectedIds = selectedCategories.map { it.id }.toSet()
            val updatedCategories = currentCategories.map { category ->
                category.copy(isSelected = selectedIds.contains(category.id))
            }
            _uiState.value = UiState.Success(updatedCategories)
        }
    }

    /**
     * Returns the currently selected categories
     */
    fun getSelectedCategories(): List<Category> {
        return _selectedCategories.value
    }
}