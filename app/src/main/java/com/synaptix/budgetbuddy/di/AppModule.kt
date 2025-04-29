package com.synaptix.budgetbuddy.di

import android.content.Context
import androidx.room.Room
import com.synaptix.budgetbuddy.core.usecase.AddTransactionUseCase
import com.synaptix.budgetbuddy.data.AppDatabase
import com.synaptix.budgetbuddy.data.local.UserDao
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
//    @Provides
//    @Singleton
//    fun provideSignUpUseCase(): SignUpUseCase {
//        return SignUpUseCase()
//    }

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
        ).build()
    }
}
