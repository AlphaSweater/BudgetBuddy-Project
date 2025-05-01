package com.synaptix.budgetbuddy.presentation.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.usecase.auth.SignupUserUseCase
import com.synaptix.budgetbuddy.data.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signupUserUseCase: SignupUserUseCase
): ViewModel() {
    //function that instantiates the UserEntity object
    //takes email and password from the viewModel
    fun signUp(email: String, password: String) {
        //hashed password using BCrypt
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        val userEntity = UserEntity(
            user_id = 0,
            firstName = null,
            lastName = null,
            email = email,
            password = hashedPassword
        )

        // Call the use case to sign up the user
        viewModelScope.launch {
            val result = signupUserUseCase.execute(userEntity)
        }
    }
    suspend fun checkEmailExists(email: String): Boolean {
        return signupUserUseCase.emailExists(email)
    }
}