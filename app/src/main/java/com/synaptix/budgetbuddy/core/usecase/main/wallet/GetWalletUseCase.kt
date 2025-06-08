package com.synaptix.budgetbuddy.core.usecase.main.wallet

import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.model.getOrReturn
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import javax.inject.Inject

class GetWalletUseCase @Inject constructor(
    private val userRepository: FirestoreUserRepository,
    private val walletRepository: FirestoreWalletRepository
) {
    sealed class GetWalletResult {
        data class Success(val wallet: Wallet) : GetWalletResult()
        data class Error(val message: String) : GetWalletResult()
    }

    suspend fun execute(userId: String, walletId: String): GetWalletResult {
        if (userId.isEmpty()) {
            return GetWalletResult.Error("Invalid user ID")
        }

        return try {
            val user = userRepository.getUserProfile(userId).getOrReturn {
                return GetWalletResult.Error("Error fetching user: $it")
            } ?: return GetWalletResult.Error("User not found")

            val dto = walletRepository.getWalletById(userId, walletId).getOrReturn {
                return GetWalletResult.Error("Error fetching wallet: $it")
            } ?: return GetWalletResult.Error("Wallet not found")

            val domainUser = user.toDomain()
            val wallet = dto.toDomain(
                user = domainUser
            )

            GetWalletResult.Success(wallet)

        } catch (e: Exception) {
            GetWalletResult.Error("Unexpected error: ${e.message}")
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\