package com.synaptix.budgetbuddy.core.usecase.auth

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetVersionUseCase @Inject constructor(
    private val context: Context
) {
    suspend fun execute(): String {
        return withContext(Dispatchers.IO) {
            try {
                context.assets.open("version.txt").bufferedReader().use { it.readText().trim() }
            } catch (e: Exception) {
                "Version file not found"
            }
        }
    }
}