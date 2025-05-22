package com.synaptix.budgetbuddy.data.firebase.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class WalletDTO(
    @DocumentId
    val id: String = "", // Firestore document ID
    val userId: String = "", // Firestore user ID
    val name: String = "",
    val currency: String = "ZAR", // Default to ZAR
    val balance: Double = 0.0,
    val excludeFromTotal: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastTransactionAt: Long? = null // Track when the last transaction occurred
) {
    // helper methods

    // Method to update the balance
    fun updateBalance(newBalance: Double): WalletDTO {
        return this.copy(
            balance = newBalance,
            updatedAt = System.currentTimeMillis()
        )
    }
}