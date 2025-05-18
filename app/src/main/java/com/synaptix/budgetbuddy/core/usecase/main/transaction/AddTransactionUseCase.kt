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

import com.synaptix.budgetbuddy.core.model.TransactionIn
import com.synaptix.budgetbuddy.data.entity.mapper.toEntity
import com.synaptix.budgetbuddy.data.repository.ITransactionRepository
import com.synaptix.budgetbuddy.data.repository.Result
import javax.inject.Inject

// UseCase class for adding a new transaction
class AddTransactionUseCase @Inject constructor(
    // Injecting the TransactionRepository to handle transaction-related operations
    private val repository: ITransactionRepository
) {
    sealed class AddTransactionResult {
        data class Success(val transactionId: Long) : AddTransactionResult()
        data class Error(val message: String) : AddTransactionResult()
    }

    // Executes the operation to add a new transaction by converting to entity and inserting into the repository
    suspend fun execute(transaction: TransactionIn): AddTransactionResult {
        // Input validation
        if (transaction.amount <= 0) {
            return AddTransactionResult.Error("Amount must be greater than 0")
        }

        if (transaction.date.isBlank()) {
            return AddTransactionResult.Error("Date cannot be empty")
        }

        if (transaction.currencyType.isBlank()) {
            return AddTransactionResult.Error("Currency type cannot be empty")
        }

        // Convert to entity and attempt to insert
        return when (val result = repository.insertTransaction(transaction.toEntity())) {
            is Result.Success -> {
                println("Transaction added successfully: $transaction")
                AddTransactionResult.Success(result.data)
            }
            is Result.Error -> {
                println("Failed to add transaction: ${result.exception.message}")
                AddTransactionResult.Error("Failed to add transaction: ${result.exception.message}")
            }
        }
    }
}
