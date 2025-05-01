package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.categorySelectorPopUp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetCategoriesUseCase
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategorySelectorViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase
): ViewModel() {
    private val _categories = MutableLiveData<List<CategoryEntity>>()
    val categories: LiveData<List<CategoryEntity>> get() = _categories

    fun loadCategories(userId: Int) {
        viewModelScope.launch {
            val result = getCategoriesUseCase.invoke(userId)
            _categories.value = result
        }
    }
}