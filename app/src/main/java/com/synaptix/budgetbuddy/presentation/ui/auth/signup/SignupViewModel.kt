package com.synaptix.budgetbuddy.presentation.ui.auth.signup

import androidx.lifecycle.ViewModel
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.core.usecase.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
): ViewModel() {
    fun signUp(
        email: String,
        password: String
    ) {
        val user = User(
            userId = 0,
            name = null,
            surname = null,
            email = email,
            password = password
        )
        signUpUseCase.execute(user)
    }

}