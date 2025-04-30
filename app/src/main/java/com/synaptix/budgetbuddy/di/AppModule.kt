package com.synaptix.budgetbuddy.di

import android.content.Context
import androidx.room.Room
import com.synaptix.budgetbuddy.data.repository.UserRepository
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddTransactionUseCase
import com.synaptix.budgetbuddy.core.usecase.auth.LoginUseCase
import com.synaptix.budgetbuddy.data.AppDatabase
import com.synaptix.budgetbuddy.data.local.UserDao
import com.synaptix.budgetbuddy.data.local.WalletDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAddTransactionUseCase(): AddTransactionUseCase {
        return AddTransactionUseCase()
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    //AI assisted with this logic
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "budgetbuddy_db"
        )   .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(repository: UserRepository): LoginUseCase {
        return LoginUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideWalletDao(appDatabase: AppDatabase): WalletDao {
        return appDatabase.walletDao()
    }

}
