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

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.auth.LoginUserUseCase
import com.synaptix.budgetbuddy.core.usecase.main.budget.GetBudgetsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.AddTransactionUseCase
import com.synaptix.budgetbuddy.core.usecase.main.category.GetCategoriesUseCase
import com.synaptix.budgetbuddy.core.usecase.main.display.TotalBudgetUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import com.synaptix.budgetbuddy.core.usecase.main.transaction.UploadImageUseCase
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreLabelRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreTransactionRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    // Firebase
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth


    // Provide Firebase Repositories
    @Provides
    @Singleton
    fun provideFirestoreUserRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): FirestoreUserRepository {
        return FirestoreUserRepository(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideFirestoreBudgetRepository(
        firestore: FirebaseFirestore,
        categoryRepository: FirestoreCategoryRepository
    ): FirestoreBudgetRepository {
        return FirestoreBudgetRepository(firestore, categoryRepository)
    }

    @Provides
    @Singleton
    fun provideFirestoreCategoryRepository(
        firestore: FirebaseFirestore
    ): FirestoreCategoryRepository {
        return FirestoreCategoryRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideFirestoreTransactionRepository(
        firestore: FirebaseFirestore
    ): FirestoreTransactionRepository {
        return FirestoreTransactionRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideFirestoreWalletRepository(
        firestore: FirebaseFirestore
    ): FirestoreWalletRepository {
        return FirestoreWalletRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideFirestoreLabelRepository(
        firestore: FirebaseFirestore
    ): FirestoreLabelRepository {
        return FirestoreLabelRepository(firestore)
    }

    // Provide Use Cases
    @Provides
    @Singleton
    fun provideGetUserIdUseCase(repository: FirestoreUserRepository): GetUserIdUseCase {
        return GetUserIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(repository: FirestoreUserRepository): LoginUserUseCase {
        return LoginUserUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddTransactionUseCase(transactionRepository: FirestoreTransactionRepository, walletRepository: FirestoreWalletRepository): AddTransactionUseCase {
        return AddTransactionUseCase(transactionRepository, walletRepository)
    }

    @Provides
    @Singleton
    fun provideGetTransactionsUseCase(
        transactionRepository: FirestoreTransactionRepository,
        userRepository: FirestoreUserRepository,
        walletRepository: FirestoreWalletRepository,
        categoryRepository: FirestoreCategoryRepository,
        labelRepository: FirestoreLabelRepository
    ): GetTransactionsUseCase {
        return GetTransactionsUseCase(
            transactionRepository,
            userRepository,
            walletRepository,
            categoryRepository,
            labelRepository
        )
    }

    @Provides
    @Singleton
    fun provideGetCategoriesUseCase(categoryRepository: FirestoreCategoryRepository, userRepository: FirestoreUserRepository): GetCategoriesUseCase {
        return GetCategoriesUseCase(categoryRepository, userRepository)
    }

    @Provides
    @Singleton
    fun provideGetBudgetsUseCase(budgetRepository: FirestoreBudgetRepository, userRepository: FirestoreUserRepository, categoryRepository: FirestoreCategoryRepository): com.synaptix.budgetbuddy.core.usecase.main.budget.GetBudgetsUseCase {
        return GetBudgetsUseCase(budgetRepository, userRepository, categoryRepository)
    }

    @Provides
    @Singleton
    fun provideUploadImageUseCase() : UploadImageUseCase {
        return UploadImageUseCase()
    }

    @Provides
    @Singleton
    fun provideTotalBudgetUseCase(budgetRepository: FirestoreBudgetRepository, userRepository: FirestoreUserRepository, categoryRepository: FirestoreCategoryRepository): TotalBudgetUseCase {
        return TotalBudgetUseCase(budgetRepository, userRepository, categoryRepository)
    }
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

