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

package com.synaptix.budgetbuddy.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.data.repository.UserRepository
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddTransactionUseCase
import com.synaptix.budgetbuddy.core.usecase.auth.LoginUserUseCase
import com.synaptix.budgetbuddy.data.AppDatabase
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.local.CategoryDatabaseCallback
import com.synaptix.budgetbuddy.data.local.LabelDatabaseCallback
import com.synaptix.budgetbuddy.data.local.dao.BudgetDao
import com.synaptix.budgetbuddy.data.local.dao.CategoryDao
import com.synaptix.budgetbuddy.data.local.dao.LabelDao
import com.synaptix.budgetbuddy.data.local.dao.TransactionDao
import com.synaptix.budgetbuddy.data.local.dao.UserDao
import com.synaptix.budgetbuddy.data.local.dao.WalletDao
import com.synaptix.budgetbuddy.data.local.datastore.DataStoreManager
import com.synaptix.budgetbuddy.data.repository.BudgetRepository
import com.synaptix.budgetbuddy.data.repository.LabelRepository
import com.synaptix.budgetbuddy.data.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

// Dagger module to provide application-level dependencies

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provide AddTransactionUseCase for transaction-related operations
    @Provides
    @Singleton
    fun provideAddTransactionUseCase(repository: TransactionRepository): AddTransactionUseCase {
        return AddTransactionUseCase(repository)
    }

    // Provide GetUserIdUseCase for fetching the user ID
    @Provides
    @Singleton
    fun provideGetUserIdUseCase(repository: UserRepository): GetUserIdUseCase {
        return GetUserIdUseCase(repository)
    }

    // Provide the UserDao for accessing user-related data in the database
    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    // Provide the AppDatabase instance for Room database setup
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        lateinit var db: AppDatabase
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "budgetbuddy_db"
        )
            // this destroys database if version changes to allow for easier migration when testing and developing app
            .fallbackToDestructiveMigration()

            // AI assisted with callback to allow for default categories to be added to the database on creation
            .addCallback(CategoryDatabaseCallback { db })
            .addCallback(LabelDatabaseCallback { db })
            .build()
        return db
    }

    // Provide UserRepository for managing user data in the database and DataStore
    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao, dataStoreManager: DataStoreManager): UserRepository {
        return UserRepository(userDao, dataStoreManager)
    }

    // Provide DataStoreManager for managing application preferences and data
    @Provides
    @Singleton
    fun providesDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    // Provide LoginUserUseCase for handling user login functionality
    @Provides
    @Singleton
    fun provideLoginUseCase(repository: UserRepository): LoginUserUseCase {
        return LoginUserUseCase(repository)
    }

    // Provide WalletDao for accessing wallet data in the database
    @Provides
    @Singleton
    fun provideWalletDao(appDatabase: AppDatabase): WalletDao {
        return appDatabase.walletDao()
    }

    // Provide CategoryDao for accessing category data in the database
    @Provides
    @Singleton
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    // Provide LabelDao for accessing label data in the database
    @Provides
    @Singleton
    fun provideLabelDao(appDatabase: AppDatabase): LabelDao {
        return appDatabase.labelDao()
    }

    // Provide LabelRepository for managing label-related data
    @Provides
    @Singleton
    fun providelabelrepository(labelDao: LabelDao): LabelRepository {
        return LabelRepository(labelDao)
    }

    // Provide TransactionDao for accessing transaction data in the database
    @Provides
    @Singleton
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }

    // Provide TransactionRepository for managing transaction-related data
    @Provides
    @Singleton
    fun provideTransactionRepository(transactionDao: TransactionDao): TransactionRepository {
        return TransactionRepository(transactionDao)
    }

    // Provide BudgetDao for accessing budget data in the database
    @Provides
    @Singleton
    fun provideBudgetDao(appDatabase: AppDatabase): BudgetDao {
        return appDatabase.budgetDao()
    }

    // Provide BudgetRepository for managing budget-related data
    @Provides
    @Singleton
    fun provideBudgetRepository(budgetDao: BudgetDao): BudgetRepository {
        return BudgetRepository(budgetDao)
    }
}
