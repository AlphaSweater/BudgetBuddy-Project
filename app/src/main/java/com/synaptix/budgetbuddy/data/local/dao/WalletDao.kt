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

package com.synaptix.budgetbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import com.synaptix.budgetbuddy.data.entity.relations.WalletWithUser

@Dao
interface WalletDao {

    // Inserts a wallet into the database. If the wallet already exists, it ignores the operation.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    //sql query to grab a wallet based on wallet ID
    @Query("SELECT * FROM wallet_table WHERE wallet_id = :walletId")
    suspend fun getWalletById(walletId: Int): WalletEntity?

    //sql query to grab all wallets based on user ID
    @Query("SELECT * FROM wallet_table WHERE user_id = :userId")
    suspend fun getWalletsByUserId(userId: Int): List<WalletWithUser>
}