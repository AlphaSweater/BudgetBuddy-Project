// ======================================================================================
// Group 2 - Group Members:
// ======================================================================================
// * Chad Fairlie ST10269509
// * Dhiren Ruthenavelu ST10256859
// * Kayla Ferreira ST10259527
// * Nathan Teixeira ST10249266
// ======================================================================================
// Declaration:
// ======================================================================================
// We declare that this work is our own original work and that no part of it has been
// copied from any other source, except where explicitly acknowledged.
// ======================================================================================
// References:
// ======================================================================================
// * ChatGPT was used to help with the design and planning. As well as assisted with
//   finding and fixing errors in the code.
// * ChatGPT also helped with the forming of comments for the code.
// * https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
// ======================================================================================

package com.synaptix.budgetbuddy.data.local.dao

import androidx.room.*
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.relations.TransactionWithDetail

@Dao
interface TransactionDao {

    // Fetches all transactions for a specific user based on user_id.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transaction_table WHERE transaction_id = :transactionId")
    suspend fun deleteById(transactionId: Int)

    @Query("SELECT * FROM transaction_table WHERE transaction_id = :id")
    suspend fun getTransactionById(id: Int): TransactionWithDetail?

    @Query("SELECT * FROM transaction_table WHERE user_id = :userId")
    suspend fun getTransactionsForUser(userId: Int): List<TransactionEntity>

    @Query("SELECT * FROM transaction_table WHERE user_id = :userId")
    suspend fun getTransactionsWithDetail(userId: Int): List<TransactionWithDetail>

    @Query("""
        SELECT * FROM transaction_table 
        WHERE user_id = :userId 
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    suspend fun getTransactionsForUserInDateRange(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<TransactionWithDetail>

    @Query("""
        SELECT SUM(amount) FROM transaction_table 
        WHERE user_id = :userId AND category_id = :categoryId
    """)
    suspend fun getTotalAmountByCategory(userId: Int, categoryId: Int): Double?

    @Query("""
        SELECT SUM(amount) FROM transaction_table 
        WHERE user_id = :userId AND wallet_id = :walletId
    """)
    suspend fun getTotalAmountByWallet(userId: Int, walletId: Int): Double?
}