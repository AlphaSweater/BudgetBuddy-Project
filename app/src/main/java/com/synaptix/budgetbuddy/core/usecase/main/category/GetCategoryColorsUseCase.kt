package com.synaptix.budgetbuddy.core.usecase.main.category

import com.synaptix.budgetbuddy.core.model.CategoryColor
import com.synaptix.budgetbuddy.data.repository.CategoryAssetsRepository
import javax.inject.Inject

class GetCategoryColorsUseCase @Inject constructor(
    private val repository: CategoryAssetsRepository
) {
    suspend fun execute(): List<CategoryColor> {
        return repository.getAllColors()
    }
}