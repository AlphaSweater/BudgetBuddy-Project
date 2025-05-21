package com.synaptix.budgetbuddy.data.firebase.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class TransactionDTO(
    @DocumentId
    val id: String = "", // Firestore document ID
    val userId: String = "", // Firestore user ID
    val walletId: String = "", // Firestore wallet ID
    val categoryId: String = "", // Firestore category ID
    val labelIds: List<String> = emptyList(), // List of label IDs associated with this transaction
    val amount: Double = 0.0,
    val currency: String = "ZAR", // Default to ZAR
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val photo: String? = null, // URL to the photo in Firebase Storage
    val recurrenceRate: String? = null, // e.g., "daily", "weekly", "monthly", "yearly"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val nextOccurrence: Long? = null // When the next recurring transaction should occur
)