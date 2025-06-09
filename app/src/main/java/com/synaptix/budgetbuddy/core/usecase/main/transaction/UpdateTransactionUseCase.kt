package com.synaptix.budgetbuddy.core.usecase.main.transaction

import android.util.Log
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreTransactionRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: FirestoreTransactionRepository,
    private val walletRepository: FirestoreWalletRepository
) {
    sealed class UpdateTransactionResult {
        data class Success(val transactionId: String) : UpdateTransactionResult()
        data class Error(val message: String) : UpdateTransactionResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to update an existing transaction
    fun execute(updatedTransaction: Transaction, oldTransaction: Transaction): Flow<UpdateTransactionResult> = flow {
        try {
            // Convert domain model to DTO using mapper
            val updatedTransactionDTO = updatedTransaction.toDTO()

            // Update the transaction
            when (val result = transactionRepository.updateTransaction(updatedTransaction.user.id, updatedTransactionDTO)) {
                is Result.Success -> {
                    Log.d("UpdateTransactionUseCase", "Transaction updated successfully: ${updatedTransaction.id}")
                    // Update wallet balance
                    updateWalletBalance(updatedTransaction, oldTransaction)
                    emit(UpdateTransactionResult.Success(updatedTransaction.id))
                }
                is Result.Error -> {
                    Log.e("UpdateTransactionUseCase", "Error updating transaction: ${result.exception.message}")
                    emit(UpdateTransactionResult.Error("Failed to update transaction: ${result.exception.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateTransactionUseCase", "Exception while updating transaction: ${e.message}")
            emit(UpdateTransactionResult.Error("Failed to update transaction: ${e.message}"))
        }
    }

    private suspend fun updateWalletBalance(newTransaction: Transaction, oldTransaction: Transaction) {
        val walletDTO = newTransaction.wallet.toDTO()
        val currentTime = System.currentTimeMillis()

        // First, reverse the effect of the old transaction
        val balanceAfterReversal = when (oldTransaction.category.type.uppercase()) {
            "INCOME" -> walletDTO.balance - oldTransaction.amount
            else -> walletDTO.balance + oldTransaction.amount
        }

        // Then, apply the effect of the new transaction
        val updatedBalance = when (newTransaction.category.type.uppercase()) {
            "INCOME" -> balanceAfterReversal + newTransaction.amount
            else -> balanceAfterReversal - newTransaction.amount
        }

        val updatedWalletDTO = walletDTO.copy(
            balance = updatedBalance,
            lastTransactionAt = currentTime
        )

        when (val result = walletRepository.updateWallet(newTransaction.user.id, updatedWalletDTO)) {
            is Result.Success -> {
                Log.d("UpdateTransactionUseCase", "Wallet balance updated successfully: ${result.data}")
            }
            is Result.Error -> {
                Log.e("UpdateTransactionUseCase", "Error updating wallet balance: ${result.exception.message}")
            }
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\ 