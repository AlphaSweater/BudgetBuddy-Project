package com.synaptix.budgetbuddy.data.firebase.model

import com.google.firebase.firestore.DocumentId
import com.synaptix.budgetbuddy.core.model.RecurrenceData

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
    val photoUrl: String? = null, // URL to the photo in Firebase Storage
    val recurrenceData: RecurrenceDataDTO = RecurrenceDataDTO.DEFAULT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val nextOccurrence: Long? = null // When the next recurring transaction should occur
)

data class RecurrenceDataDTO(
    val type: String = "Once Off",
    val interval: Int = 1,
    val weekDays: List<String> = emptyList(),
    val isDayOfWeek: Boolean = false,
    val endType: String = "Never",
    val endValue: String? = null,
    val occurrenceCount: Int = 0
) {
    companion object {
        val DEFAULT = RecurrenceDataDTO(
            type = "Once Off",
            interval = 1,
            endType = "Never"
        )
    }
}