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

import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import com.synaptix.budgetbuddy.data.repository.WalletRepository
import javax.inject.Inject

// UseCase class for fetching the wallets associated with a user
class GetWalletUseCase @Inject constructor(
    // Injecting the WalletRepository to handle wallet-related data fetching
    private val walletRepository: WalletRepository
) {
    // Executes the operation to get wallets for the specified user
    suspend fun execute(userId: Int): List<Wallet> {
        // Fetches the list of wallets for the given user from the repository
        return walletRepository.getWalletsByUserId(userId)
    }
}
