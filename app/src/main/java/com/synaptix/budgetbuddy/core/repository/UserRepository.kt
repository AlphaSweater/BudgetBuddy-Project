package com.synaptix.budgetbuddy.core.repository

import com.synaptix.budgetbuddy.data.local.UserDao
import com.synaptix.budgetbuddy.data.entity.UserEntity
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }
}