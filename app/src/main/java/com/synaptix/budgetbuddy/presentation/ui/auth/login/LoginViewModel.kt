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
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel to manage the UI state for Login
sealed class LoginUiState {
    object Idle : LoginUiState() // The idle state, waiting for user input
    object Loading : LoginUiState() // The loading state, showing progress
    object Success : LoginUiState() // The success state, login successful
    data class Error(val message: String) : LoginUiState() // The error state with an error message
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase // Injected use case to handle the login logic
) : ViewModel() {

    // LiveData to hold the current login state
    private val _loginState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val loginState: LiveData<LoginUiState> get() = _loginState

    // Function to handle the login process
    fun login(email: String, password: String) {
        _loginState.value = LoginUiState.Loading // Set state to Loading before login attempt

        viewModelScope.launch {
            val result = loginUserUseCase(email, password) // Call the use case to perform login
            // Update the state based on the result of the login attempt
            _loginState.value = when (result) {
                is LoginResult.Success -> LoginUiState.Success // If successful, show success
                is LoginResult.UserNotFound -> LoginUiState.Error("User not found") // User not found error
                is LoginResult.IncorrectPassword -> LoginUiState.Error("Incorrect password") // Incorrect password error
                is LoginResult.Error -> LoginUiState.Error(result.message) // General error with message
            }
        }
    }

    // Function to reset the state back to idle
    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }
}
