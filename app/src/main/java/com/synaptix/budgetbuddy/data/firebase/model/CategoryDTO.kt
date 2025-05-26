package com.synaptix.budgetbuddy.data.firebase.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class CategoryDTO(
    @DocumentId
    val id: String = "", // Firestore document ID
    val userId: String? = null, // Nullable to allow for default categories
    val name: String = "",
    val type: String = "", // e.g., "Income" or "Expense"
    val color: Int = 0, // Color resource ID
    val icon: Int = 0, // Icon resource ID
    val isDefault: Boolean = false, // Whether this is a default category
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)