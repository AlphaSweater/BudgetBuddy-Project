package com.synaptix.budgetbuddy.core.usecase.main.transaction

import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.data.repository.TransactionRepository
import javax.inject.Inject


class GetTransactionUseCase @Inject constructor(
private val transactionRepository: TransactionRepository
) {

    suspend fun execute(userid: Int): List<Transaction> {
        return transactionRepository.getTransactionsForUser(userid)
    }

}
