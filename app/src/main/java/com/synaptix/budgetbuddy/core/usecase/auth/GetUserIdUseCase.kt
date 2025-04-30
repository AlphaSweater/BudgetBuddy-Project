package com.synaptix.budgetbuddy.core.usecase.auth

import com.synaptix.budgetbuddy.data.repository.UserRepository
import javax.inject.Inject

class GetUserIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    // The UseCase should handle fetching the user ID from the repository
    suspend fun execute(): Int {
        return userRepository.getCurrentUserId()
    }
}