package com.synaptix.budgetbuddy.core.usecase.main.transaction

import com.synaptix.budgetbuddy.core.model.Transaction

class AddTransactionUseCase {
    fun execute(transaction: Transaction) {
        // For now, just print or log it.
        // Later, this will call the Repository to save it into DB
        println("Transaction added: $transaction")
    }
}