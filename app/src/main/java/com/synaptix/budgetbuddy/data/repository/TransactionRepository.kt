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
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.mapper.toDomain
import com.synaptix.budgetbuddy.data.local.dao.TransactionDao
import javax.inject.Inject

// ===================================
// TransactionRepository
// ===================================
// This repository handles operations related to transactions,
// such as inserting new transactions and fetching transactions for a specific user.
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    // ===================================
    // insertTransaction - Insert New Transaction
    // ===================================
    suspend fun insertTransaction(entity: TransactionEntity): Long {
        return transactionDao.insert(entity)
    }

    // ===================================
    // getTransactionsForUser - Fetch Transactions for User
    // ===================================
    suspend fun getTransactionsForUser(userId: Int): List<Transaction> {
        val transactions = transactionDao.getTransactionsWithDetail(userId)
        return transactions.map { it.toDomain() }
    }
}
