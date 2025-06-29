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

import android.util.Log
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

// UseCase class for fetching the wallets associated with a user
class GetWalletsUseCase @Inject constructor(
    // Injecting the WalletRepository to handle wallet-related data fetching
    private val walletRepository: FirestoreWalletRepository,
    private val userRepository: FirestoreUserRepository
) {
    sealed class GetWalletResult {
        data class Success(val wallets: List<Wallet>) : GetWalletResult()
        data class Error(val message: String) : GetWalletResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to fetch wallets for the specified user
    fun execute(userId: String): Flow<GetWalletResult> {
        if (userId.isEmpty()) {
            return kotlinx.coroutines.flow.flow { 
                emit(GetWalletResult.Error("Invalid user ID")) 
            }
        }

        return combine(
            userRepository.observeUserProfile(userId),
            walletRepository.observeWalletsForUser(userId)
        ) { user, wallets ->
            if (user == null) {
                return@combine GetWalletResult.Error("User not found")
            }

            val domainUser = user.toDomain()

            val fullWallets = wallets.mapNotNull { dto ->
                try {
                    dto.toDomain(domainUser)
                } catch (e: Exception) {
                    Log.e("GetWalletUseCase", "Error converting a wallet DTO to domain: ${e.message}")
                    null
                }
            }

            GetWalletResult.Success(fullWallets)
        }.flowOn(Dispatchers.IO)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to observe total balance for the specified user
    fun observeTotalBalance(userId: String): Flow<Double> {
        return walletRepository.observeTotalBalance(userId)
            .flowOn(Dispatchers.IO)
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
