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

// ===================================
// AppDatabase
// ===================================
// This class represents the Room database for the application. It defines the database
// structure and includes all the entities (tables) for the application such as User,
// Wallet, Category, Budget, etc. The database version is 9, which may change as the
// application evolves and requires migrations.
@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        WalletEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        LabelEntity::class,
        TransactionLabelEntity::class,
        MinMaxGoalEntity::class],
    version = 15
)


abstract class AppDatabase : RoomDatabase() {

    // ===================================
    // DAOs - Data Access Objects
    // ===================================
    // These abstract functions are used to interact with the database and perform CRUD operations
    // on the corresponding entities (tables).
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun labelDao(): LabelDao
    abstract fun minMaxGoalsDao(): MinMaxGoalsDao
}
