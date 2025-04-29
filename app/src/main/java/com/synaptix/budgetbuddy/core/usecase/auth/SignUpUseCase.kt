package com.synaptix.budgetbuddy.core.usecase.auth

import com.synaptix.budgetbuddy.core.model.User
class SignUpUseCase {
    fun execute(user: User) {
        println("Transaction added: $user")
    }

}