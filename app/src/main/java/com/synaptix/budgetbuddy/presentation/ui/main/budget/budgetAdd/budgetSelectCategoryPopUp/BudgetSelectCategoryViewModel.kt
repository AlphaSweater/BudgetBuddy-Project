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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel class to handle the logic for loading budget categories
@HiltViewModel
class BudgetSelectCategoryViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase, // Use case to get categories
    private val getUserIdUseCase: GetUserIdUseCase           // Use case to get user ID
) : ViewModel() {

    // Backing field for category list, MutableLiveData allows us to update data internally
    private val _categories = MutableLiveData<List<Category>>()

    // Publicly exposed immutable LiveData to observe category list
    val categories: LiveData<List<Category>> get() = _categories

    // Function to load categories for the currently logged-in user
    fun loadCategories() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute() // Get the user ID
            val result = getCategoriesUseCase.invoke(userId) // Fetch categories for that user
            _categories.value = result // Update LiveData with the fetched categories
        }
    }
}
