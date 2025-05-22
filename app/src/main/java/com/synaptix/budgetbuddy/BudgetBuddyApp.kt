package com.synaptix.budgetbuddy

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class BudgetBuddyApp : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d("FirebaseCheck", "App initialized: ${FirebaseApp.getInstance().name}")
        initializeAssets()
    }

    private fun initializeAssets() {
        applicationScope.launch {
            try {
                // Your asset initialization code here
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
