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

import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import com.synaptix.budgetbuddy.data.entity.mapper.toDomain
import com.synaptix.budgetbuddy.data.local.dao.WalletDao
import javax.inject.Inject

// ===================================
// WalletRepository
// ===================================
// This repository handles operations related to Wallets, such as fetching all wallets
// associated with a user by their user ID.
class WalletRepository @Inject constructor(
    private val walletDao: WalletDao
) {

    // ===================================
    // getWalletsByUserId - Fetch Wallets for a User
    // ===================================
    suspend fun getWalletsByUserId(userId: Int): List<Wallet> {
        // Fetch wallets from the local database based on the user ID
        val wallets = walletDao.getWalletsByUserId(userId)
        // Map the WalletEntity objects to domain models
        return wallets.map { it.toDomain() }
    }
}
