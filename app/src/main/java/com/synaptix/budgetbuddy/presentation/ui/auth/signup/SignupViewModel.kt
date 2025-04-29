package com.synaptix.budgetbuddy.presentation.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.usecase.auth.SignUpUseCase
import com.synaptix.budgetbuddy.data.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
): ViewModel() {
    //function that instantiates the UserEntity object
    //takes email and password from the viewModel
    fun signUp(email: String, password: String) {
        val userEntity = UserEntity(
            user_id = 0,
            name = null,
            surname = null,
            email = email,
            password = password
        )

        // Call the use case to sign up the user
        viewModelScope.launch {
            val result = signUpUseCase.execute(userEntity)
        }
    }

}