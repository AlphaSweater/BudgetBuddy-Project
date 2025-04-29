package com.synaptix.budgetbuddy.core.usecase.auth


import com.synaptix.budgetbuddy.data.entity.UserDao
import com.synaptix.budgetbuddy.data.local.UserEntity
import javax.inject.Inject

//AI assisted with the injection logic
class SignUpUseCase @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun execute(user: UserEntity): Long {
        return userDao.insert(user)
    }
}