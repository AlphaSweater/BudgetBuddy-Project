package com.synaptix.budgetbuddy

import android.app.Application
import com.synaptix.budgetbuddy.core.usecase.main.category.InitializeCategoryAssetsUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BudgetBuddyApp : Application() {
    // This class is the entry point for Hilt dependency injection.
    // You can add any application-wide initialization logic here if needed.

    @Inject
    lateinit var initializeCategoryAssetsUseCase: InitializeCategoryAssetsUseCase

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        initializeCategoryAssets()
    }

    private fun initializeCategoryAssets() {
        applicationScope.launch {
            try {
                initializeCategoryAssetsUseCase.execute()
            } catch (e: Exception) {
                // Handle any initialization errors
                e.printStackTrace()
            }
        }
    }
}
