package com.synaptix.budgetbuddy.core.usecase.auth

import com.synaptix.budgetbuddy.core.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun execute(email: String, password: String): Boolean {
        val user = userRepository.getUserByEmail(email)
        return user?.password == password
    }
}