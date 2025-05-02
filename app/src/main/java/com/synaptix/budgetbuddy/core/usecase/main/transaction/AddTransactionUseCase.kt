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
import com.synaptix.budgetbuddy.data.repository.TransactionRepository
import javax.inject.Inject

// UseCase class for adding a new transaction
class AddTransactionUseCase @Inject constructor(
    // Injecting the TransactionRepository to handle transaction-related operations
    private val repository: TransactionRepository
) {
    // Executes the operation to add a new transaction by converting to entity and inserting into the repository
    suspend fun execute(transaction: TransactionIn) {
        // Convert the TransactionIn data model to the entity representation for database insertion
        repository.insertTransaction(transaction.toEntity())

        // Logging the added transaction details to logcat for debugging purposes
        println("Transaction added: $transaction")
    }
}
