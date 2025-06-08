package com.synaptix.budgetbuddy.core.usecase.main.transaction

import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreTransactionRepository
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.core.model.getOrReturn
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.model.TransactionDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreLabelRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTransactionUseCase @Inject constructor(
    private val transactionRepository: FirestoreTransactionRepository,
    private val userRepository: FirestoreUserRepository,
    private val walletRepository: FirestoreWalletRepository,
    private val categoryRepository: FirestoreCategoryRepository,
    private val labelRepository: FirestoreLabelRepository
) {
    sealed class GetTransactionResult {
        data class Success(val transaction: Transaction) : GetTransactionResult()
        data class Error(val message: String) : GetTransactionResult()
    }

    suspend fun execute(userId: String, transactionId: String): GetTransactionResult {
        if (userId.isEmpty()) {
            return GetTransactionResult.Error("Invalid user ID")
        }

        return try {
            coroutineScope {
                // Fetch user profile
                val userDeferred = async {
                    userRepository.getUserProfile(userId).getOrReturn {
                        return@async null
                    }
                }

                // Fetch transaction
                val transactionDeferred = async {
                    transactionRepository.getTransactionById(userId, transactionId).getOrReturn {
                        return@async null
                    }
                }

                // Wait for both user and transaction
                val user = userDeferred.await() ?: return@coroutineScope GetTransactionResult.Error("User not found")
                val dto = transactionDeferred.await() ?: return@coroutineScope GetTransactionResult.Error("Transaction not found")

                // Fetch wallet and category in parallel
                val walletDeferred = async {
                    walletRepository.getWalletById(userId, dto.walletId).getOrReturn {
                        return@async null
                    }
                }

                val categoryDeferred = async {
                    categoryRepository.getCategoryById(userId, dto.categoryId).getOrReturn {
                        return@async null
                    }
                }

                // Wait for wallet and category
                val wallet = walletDeferred.await() ?: return@coroutineScope GetTransactionResult.Error("Wallet not found")
                val category = categoryDeferred.await() ?: return@coroutineScope GetTransactionResult.Error("Category not found")

                // Fetch all labels in parallel
                val labels = dto.labelIds.map { labelId ->
                    async {
                        labelRepository.getLabelById(userId, labelId).getOrReturn {
                            return@async null
                        }
                    }
                }.awaitAll().filterNotNull()

                if (labels.size != dto.labelIds.size) {
                    return@coroutineScope GetTransactionResult.Error("Some labels could not be found")
                }

                val domainUser = user.toDomain()
                val transaction = dto.toDomain(
                    user = domainUser,
                    wallet = wallet.toDomain(domainUser),
                    category = category.toDomain(domainUser),
                    labels = labels.map { it.toDomain(domainUser) }
                )

                GetTransactionResult.Success(transaction)
            }
        } catch (e: Exception) {
            GetTransactionResult.Error("Unexpected error: ${e.message}")
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\