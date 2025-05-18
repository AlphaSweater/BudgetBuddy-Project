package com.synaptix.budgetbuddy.core.usecase.main.category

import com.synaptix.budgetbuddy.core.model.CategoryIcon
import com.synaptix.budgetbuddy.data.repository.CategoryAssetsRepository
import javax.inject.Inject

class GetCategoryIconsUseCase @Inject constructor(
    private val repository: CategoryAssetsRepository
) {
    suspend fun execute(): List<CategoryIcon> {
        return repository.getAllIcons()
    }
}