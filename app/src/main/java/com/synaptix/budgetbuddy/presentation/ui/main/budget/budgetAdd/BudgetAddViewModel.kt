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

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.data.firebase.model.BudgetDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetAddViewModel @Inject constructor(
    private val firestoreBudgetRepository: FirestoreBudgetRepository,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    data class ValidationState(
        val isNameValid: Boolean = false,
        val isAmountValid: Boolean = false,
        val isCategoryValid: Boolean = false,
        val nameError: String? = null,
        val amountError: String? = null,
        val categoryError: String? = null,
        val shouldShowErrors: Boolean = false
    )

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _validationState = MutableStateFlow(ValidationState())
    val validationState: StateFlow<ValidationState> = _validationState

    private val _budgetName = MutableStateFlow<String?>(null)
    val budgetName: StateFlow<String?> = _budgetName

    private val _budgetAmount = MutableStateFlow<Double?>(null)
    val budgetAmount: StateFlow<Double?> = _budgetAmount

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category

    private val _selectedCategories = MutableStateFlow<List<Category>>(emptyList())
    val selectedCategories: StateFlow<List<Category>> = _selectedCategories

    fun setBudgetName(name: String) {
        _budgetName.value = name
        validateForm()
    }

    fun setBudgetAmount(amount: Double?) {
        _budgetAmount.value = amount
        validateForm()
    }

    fun setCategory(category: Category?) {
        _category.value = category
        validateForm()
    }

    fun setSelectedCategories(categories: List<Category>) {
        _selectedCategories.value = categories
        setCategory(categories.firstOrNull())
        validateForm()
    }

    fun validateForm(): Boolean {
        val currentState = _validationState.value
        val isNameValid = !_budgetName.value.isNullOrBlank()
        val isAmountValid = (_budgetAmount.value ?: 0.0) > 0.0
        val isCategoryValid = _category.value != null

        _validationState.value = currentState.copy(
            isNameValid = isNameValid,
            isAmountValid = isAmountValid,
            isCategoryValid = isCategoryValid,
            nameError = if (currentState.shouldShowErrors && !isNameValid) "Please enter a budget name" else null,
            amountError = if (currentState.shouldShowErrors && !isAmountValid) "Enter a valid amount" else null,
            categoryError = if (currentState.shouldShowErrors && !isCategoryValid) "Select a category" else null
        )

        return isNameValid && isAmountValid && isCategoryValid
    }

    fun showValidationErrors() {
        _validationState.value = _validationState.value.copy(shouldShowErrors = true)
        validateForm()
    }

    fun addBudget() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                val userId = getUserIdUseCase.execute()
                val newBudgetDTO = BudgetDTO(
                    userId = userId,
                    name = _budgetName.value ?: "",
                    amount = _budgetAmount.value ?: 0.0,
                    spent = 0.0,
                    categoryIds = _selectedCategories.value.map { it.id },
                    startDate = System.currentTimeMillis()
                )

                when (val result = firestoreBudgetRepository.createBudget(userId, newBudgetDTO)) {
                    is com.synaptix.budgetbuddy.core.model.Result.Success -> {
                        reset()
                        _uiState.value = UiState.Success
                    }
                    is com.synaptix.budgetbuddy.core.model.Result.Error -> {
                        _uiState.value = UiState.Error(result.exception.message ?: "Failed to create budget")
                    }
                }
            } catch (e: Exception) {
                Log.e("BudgetAddViewModel", "Exception adding budget: ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun reset() {
        _budgetName.value = null
        _budgetAmount.value = null
        _category.value = null
        _selectedCategories.value = emptyList()
        _uiState.value = UiState.Initial
        _validationState.value = ValidationState(shouldShowErrors = false)
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\