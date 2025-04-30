package com.synaptix.budgetbuddy.core.usecase.auth


import com.synaptix.budgetbuddy.data.local.dao.UserDao
import com.synaptix.budgetbuddy.data.entity.UserEntity
import javax.inject.Inject

//AI assisted with the injection logic
class SignupUserUseCase @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun execute(user: UserEntity): Long {
        //println for logcat
        println("User to be inserted: $user")

        //returns the user object to UserDao and adds to database
        return userDao.insert(user)
    }
}
