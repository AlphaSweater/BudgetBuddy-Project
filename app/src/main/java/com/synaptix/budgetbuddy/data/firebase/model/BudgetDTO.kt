package com.synaptix.budgetbuddy.data.firebase.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class BudgetDTO(
    @DocumentId
    val id: String = "", // Firestore document ID
    val userId: String = "", // Firestore user ID
    val name: String = "",
    val amount: Double = 0.0,
    val categoryIds: List<String> = emptyList(), // List of category IDs associated with this budget
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val startDate: Long = System.currentTimeMillis(), // When the budget period starts
    val endDate: Long? = null, // When the budget period ends (null for ongoing budgets)
    val isRecurring: Boolean = false, // Whether this budget repeats
    val recurrencePeriod: String? = null // e.g., "monthly", "weekly", "yearly"
) {
    @get:Exclude
    val isActive: Boolean
        get() = endDate == null || endDate > System.currentTimeMillis()

    @get:Exclude
    val isRecurringMonthly: Boolean
        get() = isRecurring && recurrencePeriod == "monthly"

    @get:Exclude
    val isRecurringWeekly: Boolean
        get() = isRecurring && recurrencePeriod == "weekly"

    @get:Exclude
    val isRecurringYearly: Boolean
        get() = isRecurring && recurrencePeriod == "yearly"
}