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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun execute(userId: String): Flow<GetTransactionsResult> {
        if (userId.isEmpty()) return flowOf(GetTransactionsResult.Error("Invalid user ID"))

        return transactionRepository.getTransactionsForUser(userId)
            .flatMapLatest { result ->
                flow {
                    when (result) {
                        is Result.Success -> {
                            val transactions = mutableListOf<Transaction>()
                            for (dto in result.data) {
                                when (val full = getTransactionData(dto)) {
                                    is Result.Success -> transactions.add(full.data)
                                    is Result.Error -> {} // Optionally log or handle
                                }
                            }
                            emit(GetTransactionsResult.Success(transactions))
                        }
                        is Result.Error -> {
                            emit(GetTransactionsResult.Error("Failed to get transactions: ${result.exception.message}"))
                        }
                    }
                }
            }.catch { e ->
                emit(GetTransactionsResult.Error("Failed to get transactions: ${e.message}"))
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun executeWithDateRange(userId: String, startDate: Long, endDate: Long): Flow<GetTransactionsResult> {
        if (userId.isEmpty()) return flowOf(GetTransactionsResult.Error("Invalid user ID"))
        if (startDate > endDate) return flowOf(GetTransactionsResult.Error("Invalid date range"))

        return transactionRepository.getTransactionsForUserInDateRange(userId, startDate, endDate)
            .flatMapLatest { result ->
                flow {
                    when (result) {
                        is Result.Success -> {
                            val transactions = mutableListOf<Transaction>()
                            for (dto in result.data) {
                                when (val full = getTransactionData(dto)) {
                                    is Result.Success -> transactions.add(full.data)
                                    is Result.Error -> {} // Optionally log
                                }
                            }
                            emit(GetTransactionsResult.Success(transactions))
                        }
                        is Result.Error -> {
                            emit(GetTransactionsResult.Error("Failed to get transactions: ${result.exception.message}"))
                        }
                    }
                }
            }.catch { e ->
                emit(GetTransactionsResult.Error("Failed to get transactions: ${e.message}"))
            }
    }

    private suspend fun getTransactionData(transaction: TransactionDTO): Result<Transaction> {
        val user = when (val result = userRepository.getUserProfile(transaction.userId).first()) {
            is Result.Success -> result.data?.toDomain()
            is Result.Error -> return Result.Error(result.exception)
        } ?: return Result.Error(Exception("User not found"))

        val wallet = when (val result = walletRepository.getWalletById(transaction.walletId).first()) {
            is Result.Success -> result.data?.toDomain(user)
            is Result.Error -> return Result.Error(result.exception)
        } ?: return Result.Error(Exception("Wallet not found"))

        val category = when (val result = categoryRepository.getCategoryById(transaction.categoryId).first()) {
            is Result.Success -> result.data?.toDomain(user)
            is Result.Error -> return Result.Error(result.exception)
        } ?: return Result.Error(Exception("Category not found"))

        val labels = when (val result = labelRepository.getLabelsByIds(transaction.labelIds).first()) {
            is Result.Success -> result.data.map { it.toDomain(user) }
            is Result.Error -> return Result.Error(result.exception)
        }

        return Result.Success(transaction.toDomain(user, wallet, category, labels))
    }
}