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

@HiltViewModel
class BudgetSelectCategoryViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
): ViewModel() {
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

    fun loadCategories() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            val result = getCategoriesUseCase.invoke(userId)
            _categories.value = result
        }
    }
}