package com.synaptix.budgetbuddy.core.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface Entity {
    val id: String

    fun formatDate(timestamp: Long?, skipMinutesAndHours: Boolean = false): String {
        if (timestamp == null) return "N/A"

        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp

        val minuteMillis = 60 * 1000
        val hourMillis = 60 * minuteMillis
        val dayMillis = 24 * hourMillis
        val date = Date(timestamp)

        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val today = Calendar.getInstance()

        if (!skipMinutesAndHours) {
            return when {
                diffMillis < minuteMillis -> "Just now"
                diffMillis < hourMillis -> {
                    val minutes = diffMillis / minuteMillis
                    "$minutes minute${if (minutes == 1L) "" else "s"} ago"
                }
                diffMillis < dayMillis -> {
                    val hours = diffMillis / hourMillis
                    "$hours hour${if (hours == 1L) "" else "s"} ago"
                }
                else -> {
                    // Fall through to calendar-based logic
                    val daysAgo = (diffMillis / dayMillis).toInt()
                    val weeksAgo = daysAgo / 7
                    val monthsAgo = (today.get(Calendar.YEAR) - cal.get(Calendar.YEAR)) * 12 +
                            (today.get(Calendar.MONTH) - cal.get(Calendar.MONTH))

                    when {
                        daysAgo == 0 -> "Today"
                        daysAgo == 1 -> "Yesterday"
                        daysAgo in 2..6 -> "$daysAgo days ago"
                        weeksAgo == 1 -> "Last week"
                        weeksAgo in 2..4 -> "$weeksAgo weeks ago"
                        monthsAgo == 1 -> "Last month"
                        monthsAgo in 2..11 -> "$monthsAgo months ago"
                        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                    }
                }
            }
        } else {
            val daysAgo = (diffMillis / dayMillis).toInt()
            val weeksAgo = daysAgo / 7
            val monthsAgo = (today.get(Calendar.YEAR) - cal.get(Calendar.YEAR)) * 12 +
                    (today.get(Calendar.MONTH) - cal.get(Calendar.MONTH))

            return when {
                daysAgo == 0 -> "Today"
                daysAgo == 1 -> "Yesterday"
                daysAgo in 2..6 -> "$daysAgo days ago"
                weeksAgo == 1 -> "Last week"
                weeksAgo in 2..4 -> "$weeksAgo weeks ago"
                monthsAgo == 1 -> "Last month"
                monthsAgo in 2..11 -> "$monthsAgo months ago"
                else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            }
        }
    }
}