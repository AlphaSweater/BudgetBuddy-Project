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

package com.synaptix.budgetbuddy.core.usecase.main.wallet

import com.synaptix.budgetbuddy.core.model.WalletIn
import com.synaptix.budgetbuddy.data.local.dao.WalletDao
import com.synaptix.budgetbuddy.data.entity.mapper.toEntity
import javax.inject.Inject

// UseCase class for adding a wallet to the user's profile
class AddWalletUseCase @Inject constructor(
    // Injecting the WalletDao to handle wallet-related database operations
    private val walletDao: WalletDao
) {
    // Executes the operation to insert a new wallet
    suspend fun execute(wallet: WalletIn): Long {
        // Log statement for tracking the wallet to be inserted
        println("wallet to be inserted: $wallet")

        // Converts the WalletIn object to its database entity representation and inserts it into the database
        return walletDao.insertWallet(wallet.toEntity())
    }
}
