package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectCategoryPopUp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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

    init {
        viewModelScope.launch { // Launch a coroutine
            // Combine search query with UI state to filter categories
            combine(_searchQuery, _uiState) { query, state ->
                when (state) {
                    is UiState.Success -> {
                        if (query.isEmpty()) {
                            state.categories
                        } else {
                            state.categories.filter {
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
     */
    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val userId = getUserIdUseCase.execute()
                if (userId.isEmpty()) {
                    _uiState.value = UiState.Error("User ID is empty")
                    return@launch
                }

                getCategoriesUseCase.execute(userId)
                    .catch { e ->
                        Log.e("TransactionSelectCategoryViewModel", "Error in categories flow: ${e.message}")
                        _uiState.value = UiState.Error(e.message ?: "Failed to load categories")
                    }
                    .collect { result ->
                        when (result) {
                            is GetCategoriesUseCase.GetCategoriesResult.Success -> {
                                Log.d("TransactionSelectCategoryViewModel", "Categories loaded successfully: ${result.categories.size}")
                                _uiState.value = UiState.Success(result.categories)
                            }
                            is GetCategoriesUseCase.GetCategoriesResult.Error -> {
                                Log.e("TransactionSelectCategoryViewModel", "Error loading categories: ${result.message}")
                                _uiState.value = UiState.Error(result.message)
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("TransactionSelectCategoryViewModel", "Exception loading categories: ${e.message}")
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
}