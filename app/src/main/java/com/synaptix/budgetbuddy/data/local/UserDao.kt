package com.synaptix.budgetbuddy.data.local

import com.synaptix.budgetbuddy.data.entity.UserEntity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    //inerts a user into the database
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserEntity): Long

    //sql query to grab a user based on user ID
    @Query("SELECT * FROM user_table WHERE user_id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?
}