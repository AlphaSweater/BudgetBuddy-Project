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

import com.synaptix.budgetbuddy.data.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

// Sealed class to represent possible login outcomes
sealed class LoginResult {
    // Successful login result
    object Success : LoginResult()

    // User not found result
    object UserNotFound : LoginResult()

    // Incorrect password result
    object IncorrectPassword : LoginResult()

    // Error result with a message
    data class Error(val message: String) : LoginResult()
}

// UseCase class for handling user login logic
class LoginUserUseCase @Inject constructor(
    // Injecting the UserRepository dependency to interact with user data
    private val userRepository: UserRepository
) {
    // Invokes the login process with email and password parameters
    // Returns a LoginResult indicating the outcome
    suspend operator fun invoke(email: String, password: String): LoginResult {
        return try {
            // Fetch the user by email, return UserNotFound if not found
            val user = userRepository.getUserByEmail(email)
                ?: return LoginResult.UserNotFound

            // Check the password using bcrypt, if valid, set the user session
            return if (BCrypt.checkpw(password, user.password )) {
                userRepository.setUserSession(user)
                LoginResult.Success
            } else {
                // Return IncorrectPassword if the bcrypt password check fails
                LoginResult.IncorrectPassword
            }
        } catch (e: Exception) {
            // Handle exceptions and return an error result with the message
            LoginResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}
