package com.synaptix.budgetbuddy.core.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface Entity {
    val id: String

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) {
            return "N/A"
        }

        val date = Date(timestamp)
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val lastWeek = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -1) }
        val thisMonth = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }

        return when {
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
            
            calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
            
            calendar.get(Calendar.YEAR) == lastWeek.get(Calendar.YEAR) &&
            calendar.get(Calendar.WEEK_OF_YEAR) == lastWeek.get(Calendar.WEEK_OF_YEAR) -> "Last week"
            
            calendar.get(Calendar.YEAR) == thisMonth.get(Calendar.YEAR) &&
            calendar.get(Calendar.MONTH) == thisMonth.get(Calendar.MONTH) -> "This month"
            
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        }
    }
}