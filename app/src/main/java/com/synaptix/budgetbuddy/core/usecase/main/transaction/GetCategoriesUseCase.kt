package com.synaptix.budgetbuddy.core.usecase.main.transaction

import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend fun invoke(userId: Int): List<CategoryEntity> {
        return categoryRepository.getCategoriesByUserId(userId)
    }
}