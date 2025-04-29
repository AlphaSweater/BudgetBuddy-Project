package com.synaptix.budgetbuddy.data.entity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.usecase.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
): ViewModel() {
    fun signUp(email: String, password: String) {
        val userEntity = UserEntity(
            user_id = 0,
            name = null,
            surname = null,
            email = email,
            password = password
        )

        viewModelScope.launch {
            val result = signUpUseCase.execute(userEntity)
        }
    }

}