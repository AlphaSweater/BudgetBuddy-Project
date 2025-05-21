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

package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.mapper.toDomain
import com.synaptix.budgetbuddy.data.local.dao.TransactionDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.synaptix.budgetbuddy.core.model.Result

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // Simple in-memory cache
    private val cache = mutableMapOf<Int, List<Transaction>>()

    suspend fun insertTransaction(entity: TransactionEntity): Result<Long> = withContext(ioDispatcher) {
        try {
            val id = transactionDao.insert(entity)
            // Invalidate cache for this user
            cache.remove(entity.user_id)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateTransaction(transaction: TransactionEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            transactionDao.update(transaction)
            // Invalidate cache for this user
            cache.remove(transaction.user_id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteTransaction(transactionId: Int): Result<Unit> = withContext(ioDispatcher) {
        try {
            transactionDao.deleteById(transactionId)
            // Invalidate all cache since we don't know the user ID
            cache.clear()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getTransactionById(transactionId: Int): Result<Transaction?> = withContext(ioDispatcher) {
        try {
            val transaction = transactionDao.getTransactionById(transactionId)
            Result.Success(transaction?.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getTransactionsForUser(userId: Int): Result<List<Transaction>> = withContext(ioDispatcher) {
        try {
            // Check cache first
            cache[userId]?.let {
                return@withContext Result.Success(it)
            }

            // If not in cache, get from database
            val transactions = transactionDao.getTransactionsWithDetail(userId)
                .map { it.toDomain() }

            // Store in cache
            cache[userId] = transactions

            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getTransactionsForUserBudget(userId: Int, budgetId: Int): Result<List<Transaction>> = withContext(ioDispatcher) {
        try {
            val transactions = transactionDao.getTransactionsForUserByBudget(userId, budgetId)
                .map { it.toDomain() }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getTransactionsForUserInDateRange(userId: Int, startDate: String, endDate: String): Result<List<Transaction>> = withContext(ioDispatcher) {
        try {
            val transactions = transactionDao.getTransactionsForUserInDateRange(userId, startDate, endDate)
                .map { it.toDomain() }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Additional helper methods
    suspend fun getTotalAmountByCategory(userId: Int, categoryId: Int): Result<Double> = withContext(ioDispatcher) {
        try {
            val total = transactionDao.getTotalAmountByCategory(userId, categoryId) ?: 0.0
            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getTotalAmountByWallet(userId: Int, walletId: Int): Result<Double> = withContext(ioDispatcher) {
        try {
            val total = transactionDao.getTotalAmountByWallet(userId, walletId) ?: 0.0
            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Cache management
    fun clearCache() {
        cache.clear()
    }

    fun invalidateUserCache(userId: Int) {
        cache.remove(userId)
    }
}
