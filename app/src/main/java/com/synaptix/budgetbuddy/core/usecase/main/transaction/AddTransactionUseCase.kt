package com.synaptix.budgetbuddy.core.usecase.main.transaction

import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.data.local.mapper.toEntity
import com.synaptix.budgetbuddy.data.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun execute(transaction: Transaction) {
    repository.insertTransaction(transaction.toEntity())
    println("Transaction added: $transaction")
}
//    fun execute(transaction: Transaction) {
//        // For now, just print or log it.
//        // Later, this will call the Repository to save it into DB
//        println("Transaction added: $transaction")
//    }

}