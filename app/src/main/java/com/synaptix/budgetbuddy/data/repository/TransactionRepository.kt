package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.local.dao.TransactionDao
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    suspend fun insertTransaction(entity: TransactionEntity): Long {
        return transactionDao.insert(entity)
    }

    suspend fun getTransactionsForUser(userId: Int): List<TransactionEntity> {
        return transactionDao.getTransactionsForUser(userId)
    }
}