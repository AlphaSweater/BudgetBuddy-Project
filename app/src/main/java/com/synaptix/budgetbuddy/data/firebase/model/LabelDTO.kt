package com.synaptix.budgetbuddy.data.firebase.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class LabelDTO(
    @DocumentId
    val id: String = "", // Firestore document ID
    val userId: String? = null, // Nullable to allow for default labels
    val name: String = "",
    val isDefault: Boolean = false, // Whether this is a default label
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)