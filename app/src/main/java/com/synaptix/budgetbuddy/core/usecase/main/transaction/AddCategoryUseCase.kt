package com.synaptix.budgetbuddy.core.usecase.main.transaction

import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.CategoryIn
import com.synaptix.budgetbuddy.core.model.TransactionIn
import com.synaptix.budgetbuddy.data.entity.mapper.toEntity
import com.synaptix.budgetbuddy.data.repository.CategoryRepository
import com.synaptix.budgetbuddy.data.repository.TransactionRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend fun execute(categoryIn: CategoryIn): Long {
        return repository.addCategory(categoryIn.toEntity()).also {
            println("Category added with ID: $it")
        }
    }
}