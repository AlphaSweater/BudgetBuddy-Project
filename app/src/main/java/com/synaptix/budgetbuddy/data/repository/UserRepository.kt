package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.local.dao.UserDao
import com.synaptix.budgetbuddy.data.local.datastore.DataStoreManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val dataStoreManager: DataStoreManager
) {
    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun setUserSession(user: UserEntity) {
        dataStoreManager.saveUser(user)
    }

    fun getUserSession(): Flow<UserEntity?> {
        return dataStoreManager.userFlow
    }

    suspend fun getCurrentUserId(): Int {
        return dataStoreManager.getUserId()
    }

    suspend fun logoutUserSession() {
        dataStoreManager.clearUser()
    }
}
