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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

    fun execute(userId: String): Flow<GetTransactionsResult> {
        if (userId.isEmpty()) {
            return flow { emit(GetTransactionsResult.Error("Invalid user ID")) }
        }

        return combine(
            userRepository.observeUserProfile(userId),
            transactionRepository.observeTransactionsForUser(userId),
            walletRepository.observeWalletsForUser(userId),
            categoryRepository.observeCategoriesForUser(userId),
            labelRepository.observeLabelsForUser(userId)
        ) { user, transactions, wallets, categories, labels ->

            if (user == null) {
                return@combine GetTransactionsResult.Error("User not found")
            }

            val domainUser = user.toDomain()

            val walletMap = wallets.associateBy { it.id }
            val categoryMap = categories.associateBy { it.id }
            val labelMap = labels.associateBy { it.id }

            val fullTransactions = transactions.mapNotNull { dto ->
                try {
                    dto.toDomain(
                        user = domainUser,
                        wallet = walletMap[dto.walletId]!!.toDomain(domainUser),
                        category = categoryMap[dto.categoryId]!!.toDomain(domainUser),
                        labels = dto.labelIds.mapNotNull { labelId ->
                            labelMap[labelId]?.toDomain(domainUser)
                        }
                    )
                } catch (e: Exception) {
                    null
                }
            }

            GetTransactionsResult.Success(fullTransactions)
        }.flowOn(Dispatchers.IO) // Optional for heavy mapping
    }

    fun executeWithDateRange(userId: String, startDate: Long, endDate: Long): Flow<GetTransactionsResult> {
        if (userId.isEmpty()) {
            return kotlinx.coroutines.flow.flow { 
                emit(GetTransactionsResult.Error("Invalid user ID")) 
            }
        }
        if (startDate > endDate) {
            return kotlinx.coroutines.flow.flow { 
                emit(GetTransactionsResult.Error("Invalid date range")) 
            }
        }

        return combine(
            userRepository.observeUserProfile(userId),
            transactionRepository.observeTransactionsInDateRange(userId, startDate, endDate)
        ) { user, transactions ->
            when (user) {
                null -> GetTransactionsResult.Error("User not found")
                else -> {
                    val domainUser = user.toDomain()
                    val fullTransactions = transactions.mapNotNull { dto ->
                        try {
                            dto.toDomain(
                                user = domainUser,
                                wallet = walletRepository.observeWallet(dto.userId, dto.walletId).first()!!.toDomain(domainUser),
                                category = categoryRepository.observeCategory(dto.userId, dto.categoryId).first()!!.toDomain(domainUser),
                                labels = labelRepository.observeLabels(dto.userId, dto.labelIds).first()
                                    .map { it.toDomain(domainUser) }
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    GetTransactionsResult.Success(fullTransactions)
                }
            }
        }
    }

    fun observeTotalAmountInDateRange(userId: String, startDate: Long, endDate: Long): Flow<Double> {
        return transactionRepository.observeTotalAmountInDateRange(userId, startDate, endDate)
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\