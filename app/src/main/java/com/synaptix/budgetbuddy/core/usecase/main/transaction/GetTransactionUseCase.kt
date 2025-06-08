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
            val user = userRepository.getUserProfile(userId).getOrReturn {
                return GetTransactionResult.Error("Error fetching user: $it")
            } ?: return GetTransactionResult.Error("User not found")


            val dto = transactionRepository.getTransactionById(userId, transactionId).getOrReturn {
                return GetTransactionResult.Error("Error fetching transaction: $it")
            } ?: return GetTransactionResult.Error("Transaction not found")

            val wallet = walletRepository.getWalletById(userId, dto.walletId).getOrReturn {
                return GetTransactionResult.Error("Error fetching wallet: $it")
            } ?: return GetTransactionResult.Error("Wallet not found")

            val category = categoryRepository.getCategoryById(userId, dto.categoryId).getOrReturn {
                return GetTransactionResult.Error("Error fetching category: $it")
            } ?: return GetTransactionResult.Error("Category not found")

            val labels = dto.labelIds.map { labelId ->
                labelRepository.getLabelById(userId, labelId).getOrReturn {
                    return GetTransactionResult.Error("Error fetching label: $it")
                } ?: return GetTransactionResult.Error("Label not found: $labelId")
            }

            val domainUser = user.toDomain()
            val transaction = dto.toDomain(
                user = domainUser,
                wallet = wallet.toDomain(domainUser),
                category = category.toDomain(domainUser),
                labels = labels.map { it.toDomain(domainUser) }
            )

            GetTransactionResult.Success(transaction)

        } catch (e: Exception) {
            GetTransactionResult.Error("Unexpected error: ${e.message}")
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\