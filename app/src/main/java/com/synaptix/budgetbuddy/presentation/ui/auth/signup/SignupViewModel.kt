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

package com.synaptix.budgetbuddy.presentation.ui.auth.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.usecase.auth.SignupResult
import com.synaptix.budgetbuddy.core.usecase.auth.SignupUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SignupUiState {
    object Idle : SignupUiState()
    object Loading : SignupUiState()
    object Success : SignupUiState()
    data class Error(val message: String) : SignupUiState()
    data class ValidationError(
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null
    ) : SignupUiState()
}

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signupUserUseCase: SignupUserUseCase
) : ViewModel() {

    private val _signupState = MutableLiveData<SignupUiState>(SignupUiState.Idle)
    val signupState: LiveData<SignupUiState> = _signupState

    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            !password.matches(Regex(".*[A-Z].*")) -> "Password must contain at least one uppercase letter"
            !password.matches(Regex(".*[0-9].*")) -> "Password must contain at least one number"
            !password.matches(Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) -> 
                "Password must contain at least one special character"
            else -> null
        }
    }

    fun validatePasswordConfirmation(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Please confirm your password"
            confirmPassword != password -> "Passwords do not match"
            else -> null
        }
    }

    fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        val confirmPasswordError = validatePasswordConfirmation(password, confirmPassword)

        if (emailError != null || passwordError != null || confirmPasswordError != null) {
            _signupState.value = SignupUiState.ValidationError(
                emailError,
                passwordError,
                confirmPasswordError
            )
            return false
        }
        return true
    }

    // Function that handles user sign-up
    // Takes email and password, hashes the password securely
    // and creates a new UserEntity object to register the user
    fun signUp(email: String, password: String) {
        if (!validateInputs(email, password, password)) {
            return
        }

        _signupState.value = SignupUiState.Loading

        viewModelScope.launch {
            try {
                val result = signupUserUseCase.invoke(email, password)
                _signupState.value = when (result) {
                    is SignupResult.Success -> SignupUiState.Success
                    is SignupResult.EmailExists -> SignupUiState.Error("Email already in use")
                    is SignupResult.Error -> SignupUiState.Error(result.message)
                }
            } catch (e: Exception) {
                _signupState.value = SignupUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Function to check if the provided email already exists in the database
    suspend fun checkEmailExists(email: String): Boolean {
        return signupUserUseCase.emailExists(email)
    }

    fun resetState() {
        _signupState.value = SignupUiState.Idle
    }
}
