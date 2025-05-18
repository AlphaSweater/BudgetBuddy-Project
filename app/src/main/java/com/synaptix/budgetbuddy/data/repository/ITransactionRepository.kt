package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.data.entity.TransactionEntity

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

interface ITransactionRepository {
    suspend fun insertTransaction(entity: TransactionEntity): Result<Long>
    suspend fun getTransactionsForUser(userId: Int): Result<List<Transaction>>
    suspend fun updateTransaction(transaction: TransactionEntity): Result<Unit>
    suspend fun deleteTransaction(transactionId: Int): Result<Unit>
    suspend fun getTransactionById(transactionId: Int): Result<Transaction?>
    suspend fun getTransactionsForUserInDateRange(
        userId: Int,
        startDate: String,
        endDate: String
    ): Result<List<Transaction>>
} 