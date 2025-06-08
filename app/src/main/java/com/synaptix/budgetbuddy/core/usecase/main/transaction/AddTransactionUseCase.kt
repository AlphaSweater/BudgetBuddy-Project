//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.core.usecase.main.transaction

import android.util.Log
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreTransactionRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// UseCase class for adding a new transaction
class AddTransactionUseCase @Inject constructor(
    // Injecting the FirestoreTransactionRepository to handle transaction-related operations
    private val transactionRepository: FirestoreTransactionRepository,
    private val walletRepository: FirestoreWalletRepository,
    private val budgetRepository: FirestoreBudgetRepository
) {
    sealed class AddTransactionResult {
        data class Success(val transactionId: String) : AddTransactionResult()
        data class Error(val message: String) : AddTransactionResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to add a new transaction
    fun execute(newTransaction: Transaction): Flow<AddTransactionResult> = flow {
        try {
            // Convert domain model to DTO using mapper
            val newTransactionDTO = newTransaction.toDTO()

            // Create the transaction
            when (val result = transactionRepository.createTransaction(newTransaction.user.id, newTransactionDTO)) {
                is Result.Success -> {
                    Log.d("AddTransactionUseCase", "Transaction added successfully: ${result.data}")
                    // Update wallet balance
                    updateWalletBalance(newTransaction)
                    emit(AddTransactionResult.Success(result.data))
                }
                is Result.Error -> {
                    Log.e("AddTransactionUseCase", "Error adding transaction: ${result.exception.message}")
                    emit(AddTransactionResult.Error("Failed to add transaction: ${result.exception.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e("AddTransactionUseCase", "Exception while adding transaction: ${e.message}")
            emit(AddTransactionResult.Error("Failed to add transaction: ${e.message}"))
        }
    }

    private suspend fun updateWalletBalance(transaction: Transaction) {
        val walletDTO = transaction.wallet.toDTO()
        val currentTime = System.currentTimeMillis()

        val updatedBalance = when (transaction.category.type.uppercase()) {
            "INCOME" -> walletDTO.balance + transaction.amount
            else -> walletDTO.balance - transaction.amount
        }

        val updatedWalletDTO = walletDTO.copy(
            balance = updatedBalance,
            lastTransactionAt = currentTime
        )

        when (val result = walletRepository.updateWallet(transaction.user.id, updatedWalletDTO)) {
            is Result.Success -> {
                Log.d("AddTransactionUseCase", "Wallet balance updated successfully: ${result.data}")
            }
            is Result.Error -> {
                Log.e("AddTransactionUseCase", "Error updating wallet balance: ${result.exception.message}")
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to add a new budget transaction
    private fun updateBudgetSpent(transaction: Transaction) {
        val budgetCategory = transaction.category

    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\