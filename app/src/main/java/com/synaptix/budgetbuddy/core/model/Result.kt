package com.synaptix.budgetbuddy.core.model

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

// Extension function to safely unwrap a Result or return early with a custom error
inline fun <T> Result<T>.getOrReturn(onError: (String) -> Nothing): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> onError(exception.message ?: "Unknown error")
    }
}

