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

package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.local.dao.UserDao
import com.synaptix.budgetbuddy.data.local.datastore.DataStoreManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// ===================================
// UserRepository
// ===================================
// This repository manages the user-related operations, such as fetching a user by email,
// managing user session data, and logging out the user.
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val dataStoreManager: DataStoreManager
) {

    // ===================================
    // getUserByEmail - Fetch User by Email
    // ===================================
    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    // ===================================
    // setUserSession - Save User Session Data
    // ===================================
    suspend fun setUserSession(user: UserEntity) {
        dataStoreManager.saveUser(user)
    }

    // ===================================
    // getUserSession - Get User Session as Flow
    // ===================================
    fun getUserSession(): Flow<UserEntity?> {
        return dataStoreManager.userFlow
    }

    // ===================================
    // getCurrentUserId - Get Current User ID
    // ===================================
    suspend fun getCurrentUserId(): Int {
        return dataStoreManager.getUserId()
    }

    // ===================================
    // logoutUserSession - Clear User Session Data
    // ===================================
    suspend fun logoutUserSession() {
        dataStoreManager.clearUser()
    }
}
