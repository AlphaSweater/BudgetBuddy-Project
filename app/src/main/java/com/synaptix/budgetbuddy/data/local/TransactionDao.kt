package com.synaptix.budgetbuddy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.synaptix.budgetbuddy.data.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long


    //query to get all transactions for a specific user
    @Query("SELECT * FROM transaction_table WHERE user_Id = :userId")
    suspend fun getTransactionsForUser(userId: Int): List<TransactionEntity>
}