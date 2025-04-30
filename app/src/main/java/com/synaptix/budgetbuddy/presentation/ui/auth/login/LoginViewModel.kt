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

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val loginState: LiveData<LoginUiState> get() = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginUiState.Loading

        viewModelScope.launch {
            val result = loginUserUseCase(email, password)
            _loginState.value = when (result) {
                is LoginResult.Success -> LoginUiState.Success
                is LoginResult.UserNotFound -> LoginUiState.Error("User not found")
                is LoginResult.IncorrectPassword -> LoginUiState.Error("Incorrect password")
                is LoginResult.Error -> LoginUiState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }
}