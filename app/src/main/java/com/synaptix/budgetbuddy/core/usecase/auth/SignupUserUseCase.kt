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
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.core.usecase.main.defaults.InsertDefaultsUseCase
import com.synaptix.budgetbuddy.data.firebase.model.UserDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed class SignupResult {
    object Success : SignupResult()
    object EmailExists : SignupResult()
    data class Error(val message: String) : SignupResult()
}

class SignupUserUseCase @Inject constructor(
    private val userRepository: FirestoreUserRepository,
    private val insertDefaultsUseCase: InsertDefaultsUseCase
) {
    suspend operator fun invoke(email: String, password: String, firstName: String? = null, lastName: String? = null): SignupResult {
        return try {
            // 1. Check if email exists
            val emailCheck = userRepository.emailExists(email)
            if (emailCheck is Result.Success && emailCheck.data) {
                return SignupResult.EmailExists
            } else if (emailCheck is Result.Error) {
                return SignupResult.Error(emailCheck.exception.message ?: "Failed to check email")
            }

            // 2. Create user DTO
            val userData = UserDTO(
                email = email,
                firstName = firstName,
                lastName = lastName
            )

            // 3. Register user
            val registrationResult = userRepository.registerUser(email, password, userData)
            if (registrationResult is Result.Error) {
                return SignupResult.Error(registrationResult.exception.message ?: "Failed to register user")
            }

            // 4. Convert to core User model with ID
            val userId = if (registrationResult is Result.Success) { registrationResult.data.id }
                else { return SignupResult.Error("Failed to retrieve user ID after registration") }
            val user = User(id = userId, email = email, firstName = firstName, lastName = lastName)

            // 5. Insert default data
            val defaultsResult = insertDefaultsUseCase.execute(user).first()
            return when (defaultsResult) {
                is InsertDefaultsUseCase.InsertResult.Success -> SignupResult.Success
                is InsertDefaultsUseCase.InsertResult.Error -> SignupResult.Error("Registered, but failed to set up defaults: ${defaultsResult.message}")
            }

        } catch (e: Exception) {
            SignupResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    suspend fun emailExists(email: String): Boolean {
        return when (val result = userRepository.emailExists(email)) {
            is Result.Success -> result.data
            is Result.Error -> false
        }
    }
}
