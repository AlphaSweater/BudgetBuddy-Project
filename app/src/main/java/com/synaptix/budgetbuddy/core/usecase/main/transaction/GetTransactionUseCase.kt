package com.synaptix.budgetbuddy.core.usecase.main.transaction

import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.data.repository.ITransactionRepository
import com.synaptix.budgetbuddy.data.repository.Result
import javax.inject.Inject

class GetTransactionUseCase @Inject constructor(
    private val repository: ITransactionRepository
) {
    sealed class GetTransactionsResult {
        data class Success(val transactions: List<Transaction>) : GetTransactionsResult()
        data class Error(val message: String) : GetTransactionsResult()
    }

    suspend fun execute(userId: Int): GetTransactionsResult {
        return when (val result = repository.getTransactionsForUser(userId)) {
            is Result.Success -> GetTransactionsResult.Success(result.data)
            is Result.Error -> GetTransactionsResult.Error(result.exception.message ?: "Unknown error occurred")
        }
    }

    suspend fun executeWithDateRange(userId: Int, startDate: String, endDate: String): GetTransactionsResult {
        return when (val result = repository.getTransactionsForUserInDateRange(userId, startDate, endDate)) {
            is Result.Success -> GetTransactionsResult.Success(result.data)
            is Result.Error -> GetTransactionsResult.Error(result.exception.message ?: "Unknown error occurred")
        }
    }

    // Helper method if you need to get transactions with default empty list on error
    suspend fun executeWithDefault(userId: Int): List<Transaction> {
        return when (val result = execute(userId)) {
            is GetTransactionsResult.Success -> result.transactions
            is GetTransactionsResult.Error -> emptyList()
        }
    }
}
