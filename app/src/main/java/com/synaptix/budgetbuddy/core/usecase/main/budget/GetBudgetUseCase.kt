package com.synaptix.budgetbuddy.core.usecase.main.budget

import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.repository.BudgetRepository
import com.synaptix.budgetbuddy.data.repository.WalletRepository
import javax.inject.Inject

class GetBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend fun execute(userId: Int): List<Budget> {
        return budgetRepository.getBudgetsByUserId(userId)
    }
}
