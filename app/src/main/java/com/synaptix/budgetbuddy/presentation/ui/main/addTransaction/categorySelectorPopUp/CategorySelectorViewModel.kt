package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.categorySelectorPopUp

import androidx.lifecycle.ViewModel
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetCategoriesUseCase
import javax.inject.Inject

class CategorySelectorViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase
): ViewModel() {
}