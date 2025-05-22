package com.synaptix.budgetbuddy.core.usecase.auth

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class GetVersionUseCase @Inject constructor(
    private val context: Context
) {
    suspend fun execute(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val version = context.assets
                .open("version.txt")
                .bufferedReader()
                .use { it.readText().trim() }

            if (version.isBlank()) {
                Result.failure(IllegalStateException("Version file is empty"))
            } else {
                Result.success(version)
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}