package com.synaptix.budgetbuddy.core.usecase.auth

import com.synaptix.budgetbuddy.core.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    // Injected DAO for database access
    private val userRepository: UserRepository
) {
    suspend fun execute(email: String, password: String): Boolean {
        // Query the database for a user with matching email and password
        val user = userRepository.getUserByEmail(email)
        // return true if found, false if not
        return user?.password == password
    }
}