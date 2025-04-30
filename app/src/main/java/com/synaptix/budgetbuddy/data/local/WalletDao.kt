package com.synaptix.budgetbuddy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.synaptix.budgetbuddy.data.entity.WalletEntity

@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(wallet: WalletEntity): Long

    //sql query to grab a wallet based on wallet ID
    @Query("SELECT * FROM wallet_table WHERE wallet_id = :walletId")
    suspend fun getWalletById(walletId: Int): WalletEntity?

    //sql query to grab all wallets based on user ID
    @Query("SELECT * FROM wallet_table WHERE user_id = :userId")
    suspend fun getWalletsByUserId(userId: Int): List<WalletEntity>
}