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
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import com.synaptix.budgetbuddy.core.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

// UseCase class for adding a wallet to the user's profile
class AddWalletUseCase @Inject constructor(
    // Injecting the WalletDao to handle wallet-related database operations
    private val walletRepository: FirestoreWalletRepository
) {
    sealed class AddWalletResult {
        data class Success(val walletId: String) : AddWalletResult()
        data class Error(val message: String) : AddWalletResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to add a new wallet
    fun execute(newWallet: Wallet): Flow<AddWalletResult> = flow {
        try {
            val newWalletDTO = newWallet.toDTO()

            // Attempt to create the wallet
            when (val result = walletRepository.createWallet(newWallet.user.id, newWalletDTO)) {
                is Result.Success -> {
                    Log.d("AddWalletUseCase", "Wallet added successfully: ${result.data}")
                    emit(AddWalletResult.Success(result.data))
                }
                is Result.Error -> {
                    Log.e("AddWalletUseCase", "Error adding wallet: ${result.exception.message}")
                    emit(AddWalletResult.Error("Failed to add wallet: ${result.exception.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e("AddWalletUseCase", "Exception while adding wallet: ${e.message}")
            emit(AddWalletResult.Error("Failed to add wallet: ${e.message}"))
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
