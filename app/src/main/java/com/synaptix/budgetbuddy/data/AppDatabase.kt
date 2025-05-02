package com.synaptix.budgetbuddy.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.synaptix.budgetbuddy.data.entity.BudgetEntity
import com.synaptix.budgetbuddy.data.local.dao.CategoryDao
import com.synaptix.budgetbuddy.data.local.dao.TransactionDao
import com.synaptix.budgetbuddy.data.local.dao.UserDao
import com.synaptix.budgetbuddy.data.local.dao.WalletDao
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.LabelEntity
import com.synaptix.budgetbuddy.data.entity.MinMaxGoalEntity
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.TransactionLabelEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import com.synaptix.budgetbuddy.data.local.dao.BudgetDao
import com.synaptix.budgetbuddy.data.local.dao.LabelDao
import com.synaptix.budgetbuddy.data.local.dao.MinMaxGoalsDao

//Ai assisted with the creation of this database
@Database(
    entities = [UserEntity::class, TransactionEntity::class, WalletEntity::class, CategoryEntity::class, BudgetEntity::class, LabelEntity::class, TransactionLabelEntity::class, MinMaxGoalEntity::class],
    version = 11
)


abstract class AppDatabase : RoomDatabase() {
    // Define the DAOs that will be used in the database
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun labelDao(): LabelDao
    abstract fun minMaxGoalsDao(): MinMaxGoalsDao
}