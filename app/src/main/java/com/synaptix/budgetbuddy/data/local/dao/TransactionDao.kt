package com.synaptix.budgetbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.relations.TransactionWithDetail

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    //query to get all transactions for a specific user
    @Query("SELECT * FROM transaction_table WHERE user_Id = :userId")
    suspend fun getTransactionsForUser(userId: Int): List<TransactionEntity>

    @Query("SELECT * FROM transaction_table WHERE user_id = :userId")
    suspend fun getTransactionsWithDetail(userId: Int): List<TransactionWithDetail>
}