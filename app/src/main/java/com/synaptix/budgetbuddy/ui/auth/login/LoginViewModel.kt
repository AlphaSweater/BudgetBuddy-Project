package com.synaptix.budgetbuddy.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    fun login(email: String, password: String) {
        // TEMPORARY HARD-CODED login logic
        if (email == "admin" && password == "admin") {
            _loginResult.value = true
        } else {
            _loginResult.value = false
        }
    }
}
