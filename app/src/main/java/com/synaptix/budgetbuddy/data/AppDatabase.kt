package com.synaptix.budgetbuddy.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.synaptix.budgetbuddy.data.entity.CategoryDao
import com.synaptix.budgetbuddy.data.entity.TransactionDao
import com.synaptix.budgetbuddy.data.entity.UserDao
import com.synaptix.budgetbuddy.data.entity.WalletDao
import com.synaptix.budgetbuddy.data.local.CategoryEntity
import com.synaptix.budgetbuddy.data.local.TransactionEntity
import com.synaptix.budgetbuddy.data.local.UserEntity
import com.synaptix.budgetbuddy.data.local.WalletEntity

//Ai assisted with the creation of this database
@Database(
    entities = [UserEntity::class, TransactionEntity::class, WalletEntity::class, CategoryEntity::class],
    version = 1
)


abstract class AppDatabase : RoomDatabase() {
    // Define the DAOs that will be used in the database
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
}