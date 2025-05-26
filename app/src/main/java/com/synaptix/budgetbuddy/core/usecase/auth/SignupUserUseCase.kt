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

package com.synaptix.budgetbuddy.core.usecase.auth

import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.UserDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import javax.inject.Inject

sealed class SignupResult {
    object Success : SignupResult()
    object EmailExists : SignupResult()
    data class Error(val message: String) : SignupResult()
}

// UseCase class for handling user signup logic
class SignupUserUseCase @Inject constructor(
    private val userRepository: FirestoreUserRepository
) {
    suspend operator fun invoke(email: String, password: String, firstName: String? = null, lastName: String? = null): SignupResult {
        return try {
            // Check if email exists
            when (val emailCheck = userRepository.emailExists(email)) {
                is Result.Success -> {
                    if (emailCheck.data) {
                        return SignupResult.EmailExists
                    }
                }
                is Result.Error -> {
                    return SignupResult.Error(emailCheck.exception.message ?: "Failed to check email")
                }
            }

            // Create user data
            val userData = UserDTO(
                email = email,
                firstName = firstName,
                lastName = lastName
            )

            // Register user
            when (val result = userRepository.registerUser(email, password, userData)) {
                is Result.Success -> SignupResult.Success
                is Result.Error -> SignupResult.Error(result.exception.message ?: "Failed to register user")
            }
        } catch (e: Exception) {
            SignupResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    // Function to check if the email already exists
    suspend fun emailExists(email: String): Boolean {
        return when (val result = userRepository.emailExists(email)) {
            is Result.Success -> result.data
            is Result.Error -> false
        }
    }
}
