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
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreTransactionRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import javax.inject.Inject

// UseCase class for adding a new transaction
class AddTransactionUseCase @Inject constructor(
    // Injecting the FirestoreTransactionRepository to handle transaction-related operations
    private val repository: FirestoreTransactionRepository,
    private val walletRepository: FirestoreWalletRepository
) {
    sealed class AddTransactionResult {
        data class Success(val transactionId: String) : AddTransactionResult()
        data class Error(val message: String) : AddTransactionResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to add a new transaction
//    suspend fun execute(newTransaction: Transaction): AddTransactionResult {
//
//        // Convert domain model to DTO using mapper
//        val newTransactionDTO = newTransaction.toDTO()
//
//        // Attempt to create the transaction
//        return try {
//            when (val result = repository.createTransaction(newTransactionDTO)) {
//                is Result.Success -> {
//                    Log.d("AddTransactionUseCase", "Transaction added successfully: ${result.data}")
//                    AddTransactionResult.Success(result.data)
//                }
//                is Result.Error -> {
//                    Log.e("AddTransactionUseCase", "Error adding transaction: ${result.exception.message}")
//                    AddTransactionResult.Error("Failed to add transaction: ${result.exception.message}")
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("AddTransactionUseCase", "Exception while adding transaction: ${e.message}")
//            AddTransactionResult.Error("Failed to add transaction: ${e.message}")
//        }
//    }
//}

    suspend fun execute(newTransaction: Transaction): AddTransactionResult {
        val newTransactionDTO = newTransaction.toDTO()

        return try {
            when (val result = repository.createTransaction(newTransactionDTO)) {
                is Result.Success -> {
                    val walletId = newTransaction.wallet.id
                    val amount = newTransaction.amount
                    val isIncome = newTransaction.category.type.lowercase() == "income"

                    val balanceUpdateResult =
                        walletRepository.updateWalletBalanceTrans(walletId, amount, isIncome)

                    when (balanceUpdateResult) {
                        is Result.Success -> {
                            Log.d(
                                "AddTransactionUseCase",
                                "Transaction and wallet updated successfully"
                            )
                            AddTransactionResult.Success(result.data)
                        }

                        is Result.Error -> {
                            Log.e(
                                "AddTransactionUseCase",
                                "Wallet update failed: ${balanceUpdateResult.exception.message}"
                            )
                            AddTransactionResult.Error("Transaction saved, but wallet update failed: ${balanceUpdateResult.exception.message}")
                        }
                    }
                }

                is Result.Error -> {
                    Log.e(
                        "AddTransactionUseCase",
                        "Error adding transaction: ${result.exception.message}"
                    )
                    AddTransactionResult.Error("Failed to add transaction: ${result.exception.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("AddTransactionUseCase", "Exception while adding transaction: ${e.message}")
            return AddTransactionResult.Error("Failed to add transaction: ${e.message}")
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\