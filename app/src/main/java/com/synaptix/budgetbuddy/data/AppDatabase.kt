package com.synaptix.budgetbuddy.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.synaptix.budgetbuddy.data.local.CategoryDao
import com.synaptix.budgetbuddy.data.local.TransactionDao
import com.synaptix.budgetbuddy.data.local.UserDao
import com.synaptix.budgetbuddy.data.local.WalletDao
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity

//Ai assisted with the creation of this database
@Database(
    entities = [UserEntity::class, TransactionEntity::class, WalletEntity::class, CategoryEntity::class],
    version = 2
)


abstract class AppDatabase : RoomDatabase() {
    // Define the DAOs that will be used in the database
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
}