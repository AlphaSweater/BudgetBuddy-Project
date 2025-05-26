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
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// UseCase class for fetching the wallets associated with a user
class GetWalletUseCase @Inject constructor(
    // Injecting the WalletRepository to handle wallet-related data fetching
    private val walletRepository: FirestoreWalletRepository,
    private val userRepository: FirestoreUserRepository
) {
    sealed class GetWalletResult {
        data class Success(val wallets: List<Wallet>) : GetWalletResult()
        data class Error(val message: String) : GetWalletResult()
    }

    // Executes the operation to get wallets for the specified user
    suspend fun execute(userId: String): GetWalletResult {
        // Input validation
        if (userId.isEmpty()) {
            return GetWalletResult.Error("Invalid user ID")
        }

        return try {
            val userResult = userRepository.getUserProfile(userId)
            val user = when (userResult) {
                is Result.Success -> userResult.data!!.toDomain()
                is Result.Error -> return GetWalletResult.Error("Failed to get user data: ${userResult.exception.message}")
            }

            // Attempt to retrieve wallets from the repository
            val walletsResult = walletRepository.getWalletsForUser(userId)
            val wallets = when (walletsResult) {
                is Result.Success -> walletsResult.data.map { it.toDomain(user) }
                is Result.Error -> return GetWalletResult.Error("Failed to get wallets: ${walletsResult.exception.message}")
            }

            println("Retrieved ${wallets.size} wallets for user $userId")
            GetWalletResult.Success(wallets)
        } catch (e: Exception) {
            println("Failed to get wallets: ${e.message}")
            GetWalletResult.Error("An error occurred: ${e.message}")
        }
    }
}
