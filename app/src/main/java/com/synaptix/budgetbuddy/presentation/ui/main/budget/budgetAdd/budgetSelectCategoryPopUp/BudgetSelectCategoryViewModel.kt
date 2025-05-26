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
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoriesUseCase.GetCategoriesResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel class to handle the logic for loading budget categories
@HiltViewModel
class BudgetSelectCategoryViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase, // Use case to get categories
    private val getUserIdUseCase: GetUserIdUseCase           // Use case to get user ID
) : ViewModel() {

    // Backing field for category list, MutableStateFlow allows us to update data internally
    private val _categories = MutableStateFlow<List<Category>>(emptyList())

    // Publicly exposed immutable StateFlow to observe category list
    val categories: StateFlow<List<Category>> get() = _categories

    // Function to load categories for the currently logged-in user
    fun loadCategories() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            when (val result = getCategoriesUseCase.execute(userId)) {
                is GetCategoriesResult.Success -> {
                    _categories.value = result.categories
                }
                is GetCategoriesResult.Error -> {
                    // Handle the error — maybe show a message
                    Log.e("Categories", "Failed: ${result.message}")
                }
            }

        }
    }
}
