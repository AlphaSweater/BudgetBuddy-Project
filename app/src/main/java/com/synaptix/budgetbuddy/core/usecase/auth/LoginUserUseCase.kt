package com.synaptix.budgetbuddy.core.usecase.auth

import com.synaptix.budgetbuddy.data.repository.UserRepository
import javax.inject.Inject

sealed class LoginResult {
    object Success : LoginResult()
    object UserNotFound : LoginResult()
    object IncorrectPassword : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class LoginUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): LoginResult {
        return try {
            val user = userRepository.getUserByEmail(email)
                ?: return LoginResult.UserNotFound

            // TODO: ⚠️ WARNING: Replace with secure password hashing later
            return if (user.password == password) {
                userRepository.setUserSession(user)
                LoginResult.Success
            } else {
                LoginResult.IncorrectPassword
            }
        } catch (e: Exception) {
            LoginResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}
