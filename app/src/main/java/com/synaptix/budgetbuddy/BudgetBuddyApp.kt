package com.synaptix.budgetbuddy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BudgetBuddyApp : Application() {
    // This class is the entry point for Hilt dependency injection.
    // You can add any application-wide initialization logic here if needed.

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        initializeAssets()
    }

    private fun initializeAssets() {
        applicationScope.launch {
            try {
//                initializeCategoryAssetsUseCase.execute()
            } catch (e: Exception) {
                // Handle any initialization errors
                e.printStackTrace()
            }
        }
    }
}
