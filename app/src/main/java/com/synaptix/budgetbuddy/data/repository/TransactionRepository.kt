package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.mapper.toDomain
import com.synaptix.budgetbuddy.data.local.dao.TransactionDao
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    suspend fun insertTransaction(entity: TransactionEntity): Long {
        return transactionDao.insert(entity)
    }

    suspend fun getTransactionsForUser(userId: Int): List<Transaction> {
        val transactions = transactionDao.getTransactionsWithDetail(userId)
        return transactions.map { it.toDomain() }
    }

}