package com.synaptix.budgetbuddy.core.usecase.main.budget

import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.data.repository.BudgetRepository
import javax.inject.Inject

class GetBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend fun execute(userId: Int): List<Budget> {
        return budgetRepository.getBudgetsByUser(userId)
    }
}