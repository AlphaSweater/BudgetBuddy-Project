package com.synaptix.budgetbuddy.core.usecase.main.display

import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TotalWalletUseCase @Inject constructor(
    private val userRepository: FirestoreUserRepository,
    private val walletRepository: FirestoreWalletRepository
    )
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to fetch the total balance of all wallets for the specified user
    suspend fun execute(userId: String): Double{
        // Ensure userId is not null or empty
        if (userId.isEmpty()) {
            throw IllegalArgumentException("Invalid user ID")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch user profile based on provided userid
        val userResult = userRepository.getUserProfile(userId)
        val user = when (userResult) {
            is Result.Success -> userResult.data?.toDomain()
            is Result.Error -> throw Exception("Failed to get user data: ${userResult.exception.message}")
        }
        //checks to see if user object is not null
        if (user == null) {
            throw Exception("User not found")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch wallets based on user id
        val walletResult = walletRepository.getWalletsForUser(userId)
        val wallets = when (walletResult) {
            is Result.Success -> walletResult.data.map { it.toDomain(user) }
            is Result.Error -> throw Exception("Failed to get wallets: ${walletResult.exception.message}")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Calculate the total wallet balance by summing up the balances of all wallets
        return calculateTotalWallets(wallets)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Function to calculate the total balance of all wallets
    fun calculateTotalWallets(wallets: List<Wallet>): Double{
        return wallets.sumOf { it.balance }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\