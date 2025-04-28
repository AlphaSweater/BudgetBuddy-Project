package com.synaptix.budgetbuddy.di

import com.synaptix.budgetbuddy.core.usecase.AddTransactionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}
