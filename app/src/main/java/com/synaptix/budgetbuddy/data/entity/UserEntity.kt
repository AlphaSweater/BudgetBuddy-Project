package com.synaptix.budgetbuddy.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

//create the user entity for the database
@Entity(
    tableName = "user_table"
)
data class UserEntity (
    @PrimaryKey(autoGenerate = true) val user_id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val password: String
)