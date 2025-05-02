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
) : ViewModel() {

    // Function that handles user sign-up
    // Takes email and password, hashes the password securely
    // and creates a new UserEntity object to register the user
    fun signUp(email: String, password: String) {
        // Hash the password using BCrypt before saving it to the database
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

        // Instantiate the UserEntity object with hashed password and provided email
        val userEntity = UserEntity(
            user_id = 0,       // User ID will be auto-generated in the database
            firstName = null,  // First name is currently set to null (can be updated later)
            lastName = null,   // Last name is currently set to null (can be updated later)
            email = email,
            password = hashedPassword
        )

        // Use coroutine scope to perform database operation asynchronously
        viewModelScope.launch {
            val result = signupUserUseCase.execute(userEntity)
            // Currently, result is not handled but can be used for future error/success handling
        }
    }

    // Function to check if the provided email already exists in the database
    suspend fun checkEmailExists(email: String): Boolean {
        return signupUserUseCase.emailExists(email)
    }
}
