package com.synaptix.budgetbuddy.core.usecase.main.budget

import com.synaptix.budgetbuddy.core.model.BudgetIn
import com.synaptix.budgetbuddy.data.entity.mapper.toEntity
import com.synaptix.budgetbuddy.data.repository.BudgetRepository
import javax.inject.Inject

class AddBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend fun execute(budget: BudgetIn) {
        val entity = budget.toEntity()
        budgetRepository.insertBudget(entity)
    }
}