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

package com.synaptix.budgetbuddy.presentation.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.usecase.auth.LoginUserUseCase
import com.synaptix.budgetbuddy.core.usecase.auth.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel to manage the UI state for Login
sealed class LoginUiState {
    object Idle : LoginUiState() // The idle state, waiting for user input
    object Loading : LoginUiState() // The loading state, showing progress
    object Success : LoginUiState() // The success state, login successful
    data class Error(val message: String) : LoginUiState() // The error state with an error message
    data class ValidationError(
        val emailError: String? = null,
        val passwordError: String? = null
    ) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase // Injected use case to handle the login logic
) : ViewModel() {

    // LiveData to hold the current login state
    private val _loginState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val loginState: LiveData<LoginUiState> get() = _loginState

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
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    fun validateInputs(email: String, password: String): Boolean {
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)

        if (emailError != null || passwordError != null) {
            _loginState.value = LoginUiState.ValidationError(emailError, passwordError)
            return false
        }
        return true
    }

    // Function to handle the login process
    fun login(email: String, password: String) {
        if (!validateInputs(email, password)) {
            return
        }

        // Set loading state immediately on the main thread
        _loginState.value = LoginUiState.Loading
        android.util.Log.d("LoginViewModel", "Setting loading state")

        viewModelScope.launch {
            try {
                android.util.Log.d("LoginViewModel", "Starting login process")
                val result = loginUserUseCase(email, password)
                android.util.Log.d("LoginViewModel", "Login result: $result")
                _loginState.value = when (result) {
                    is LoginResult.Success -> LoginUiState.Success
                    is LoginResult.UserNotFound -> LoginUiState.Error("User not found")
                    is LoginResult.IncorrectPassword -> LoginUiState.Error("Incorrect password")
                    is LoginResult.Error -> LoginUiState.Error(result.message)
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Login error", e)
                _loginState.value = LoginUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Function to reset the state back to idle
    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }
}
