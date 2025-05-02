package com.synaptix.budgetbuddy.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.data.repository.UserRepository
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddTransactionUseCase
import com.synaptix.budgetbuddy.core.usecase.auth.LoginUserUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetCategoriesUseCase
import com.synaptix.budgetbuddy.data.AppDatabase
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.local.CategoryDatabaseCallback
import com.synaptix.budgetbuddy.data.local.LabelDatabaseCallback
import com.synaptix.budgetbuddy.data.local.dao.BudgetDao
import com.synaptix.budgetbuddy.data.local.dao.CategoryDao
import com.synaptix.budgetbuddy.data.local.dao.LabelDao
import com.synaptix.budgetbuddy.data.local.dao.MinMaxGoalsDao
import com.synaptix.budgetbuddy.data.local.dao.TransactionDao
import com.synaptix.budgetbuddy.data.local.dao.UserDao
import com.synaptix.budgetbuddy.data.local.dao.WalletDao
import com.synaptix.budgetbuddy.data.local.datastore.DataStoreManager
import com.synaptix.budgetbuddy.data.repository.BudgetRepository
import com.synaptix.budgetbuddy.data.repository.CategoryRepository
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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAddTransactionUseCase(repository: TransactionRepository): AddTransactionUseCase {
        return AddTransactionUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetUserIdUseCase(repository: UserRepository): GetUserIdUseCase {
        return GetUserIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }



    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        lateinit var db: AppDatabase
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "budgetbuddy_db"
        )
            //this destroys database if version changes to allow for easier migration when testing and developing app
            .fallbackToDestructiveMigration()

            //AI assisted with callback to allow for default categories to be added to the database on creation
            .addCallback(CategoryDatabaseCallback { db })
            .addCallback(LabelDatabaseCallback {db})
            .build()
        return db
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao, dataStoreManager: DataStoreManager): UserRepository {
        return UserRepository(userDao, dataStoreManager)
    }

    @Provides
    @Singleton
    fun providesDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(repository: UserRepository): LoginUserUseCase {
        return LoginUserUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideWalletDao(appDatabase: AppDatabase): WalletDao {
        return appDatabase.walletDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    @Provides
    @Singleton
    fun provideLabelDao(appDatabase: AppDatabase): LabelDao {
        return appDatabase.labelDao()
    }

    @Provides
    @Singleton
    fun providelabelrepository(labelDao: LabelDao): LabelRepository {
        return LabelRepository(labelDao)
    }

    @Provides
    @Singleton
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(transactionDao: TransactionDao): TransactionRepository {
        return TransactionRepository(transactionDao)
    }

    @Provides
    @Singleton
    fun provideBudgetDao(appDatabase: AppDatabase): BudgetDao {
        return appDatabase.budgetDao()
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(budgetDao: BudgetDao): BudgetRepository {
        return BudgetRepository(budgetDao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao, userDao: UserDao): CategoryRepository {
        return CategoryRepository(categoryDao, userDao)
    }

    @Provides
    @Singleton
    fun provideGetCategoriesUseCase(categoryRepository: CategoryRepository) : GetCategoriesUseCase {
        return GetCategoriesUseCase(categoryRepository)
    }

    @Provides
    @Singleton
    fun provideMinMaxGoalsDao(appDatabase: AppDatabase): MinMaxGoalsDao {
        return appDatabase.minMaxGoalsDao()
    }


}
