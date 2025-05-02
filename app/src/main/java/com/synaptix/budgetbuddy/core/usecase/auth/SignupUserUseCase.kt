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

package com.synaptix.budgetbuddy.core.usecase.auth

import com.synaptix.budgetbuddy.data.local.dao.UserDao
import com.synaptix.budgetbuddy.data.entity.UserEntity
import javax.inject.Inject

// UseCase class for handling user signup logic
class SignupUserUseCase @Inject constructor(
    // Injecting UserDao to interact with the local database for user operations
    private val userDao: UserDao
) {
    // Executes the user signup by inserting the user entity into the database
    suspend fun execute(user: UserEntity): Long {
        // Logging the user details to logcat for debugging purposes
        println("User to be inserted: $user")

        // Inserts the user into the database using UserDao and returns the inserted user's ID
        return userDao.insert(user)
    }

    // Checks if the email already exists in the database
    suspend fun emailExists(email: String): Boolean {
        // Queries UserDao to see if the email already exists in the database
        return userDao.emailExists(email)
    }
}
