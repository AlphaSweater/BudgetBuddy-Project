package com.synaptix.budgetbuddy.core.usecase.main.transaction

import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreTransactionRepository
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.model.TransactionDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreLabelRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: FirestoreTransactionRepository,
    private val userRepository: FirestoreUserRepository,
    private val walletRepository: FirestoreWalletRepository,
    private val categoryRepository: FirestoreCategoryRepository,
    private val labelRepository: FirestoreLabelRepository
) {
    sealed class GetTransactionsResult {
        data class Success(val transactions: List<Transaction>) : GetTransactionsResult()
        data class Error(val message: String) : GetTransactionsResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to fetch transactions for the specified user
    suspend fun execute(userId: String): GetTransactionsResult {
        if (userId.isEmpty()) {
            return GetTransactionsResult.Error("Invalid user ID")
        }

        return try {
            when (val result = transactionRepository.getTransactionsForUser(userId)) {
                is Result.Success -> {
                    val fullTransactions = result.data.mapNotNull { dto ->
                        when (val full = getTransactionData(dto)) {
                            is Result.Success -> full.data
                            is Result.Error -> null
                        }
                    }
                    GetTransactionsResult.Success(fullTransactions)
                }
                is Result.Error -> GetTransactionsResult.Error("Failed to get transactions: ${result.exception.message}")
            }
        } catch (e: Exception) {
            GetTransactionsResult.Error("Failed to get transactions: ${e.message}")
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to fetch transactions for the specified user within a date range
    suspend fun executeWithDateRange(userId: String, startDate: Long, endDate: Long): GetTransactionsResult {
        if (userId.isEmpty()) {
            return GetTransactionsResult.Error("Invalid user ID")
        }
        if (startDate > endDate) {
            return GetTransactionsResult.Error("Invalid date range")
        }

        return try {
            when (val result = transactionRepository.getTransactionsForUserInDateRange(userId, startDate, endDate)) {
                is Result.Success -> {
                    val fullTransactions = result.data.mapNotNull { dto ->
                        when (val full = getTransactionData(dto)) {
                            is Result.Success -> full.data
                            is Result.Error -> null
                        }
                    }
                    GetTransactionsResult.Success(fullTransactions)
                }
                is Result.Error -> GetTransactionsResult.Error("Failed to get transactions: ${result.exception.message}")
            }
        } catch (e: Exception) {
            GetTransactionsResult.Error("Failed to get transactions: ${e.message}")
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Helper function to get full transaction data including user, wallet, category, and labels
    private suspend fun getTransactionData(transaction: TransactionDTO): Result<Transaction> {
        val user = when (val result = userRepository.getUserProfile(transaction.userId)) {
            is Result.Success -> result.data?.toDomain()
            is Result.Error -> return Result.Error(result.exception)
        } ?: return Result.Error(Exception("User not found"))

        val wallet = when (val result = walletRepository.getWalletById(transaction.walletId)) {
            is Result.Success -> result.data?.toDomain(user)
            is Result.Error -> return Result.Error(result.exception)
        } ?: return Result.Error(Exception("Wallet not found"))

        val category = when (val result = categoryRepository.getCategoryById(transaction.categoryId)) {
            is Result.Success -> result.data?.toDomain(user)
            is Result.Error -> return Result.Error(result.exception)
        } ?: return Result.Error(Exception("Category not found"))

        val labels = when (val result = labelRepository.getLabelsByIds(transaction.labelIds)) {
            is Result.Success -> result.data.map { it.toDomain(user) }
            is Result.Error -> return Result.Error(result.exception)
        }

        return Result.Success(transaction.toDomain(user, wallet, category, labels))
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\