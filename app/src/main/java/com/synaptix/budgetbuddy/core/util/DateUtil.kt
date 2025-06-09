package com.synaptix.budgetbuddy.core.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility class for common date range calculations and formatting
 */
object DateUtil {
    /**
     * Gets the start and end timestamps for the current month
     * @return Pair of (startOfMonth, endOfMonth) timestamps in milliseconds
     */
    fun getCurrentMonthRange(): Pair<Long, Long> {
        val now = LocalDateTime.now()
        val startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
            .withHour(0).withMinute(0).withSecond(0).withNano(0)
        val endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth())
            .withHour(23).withMinute(59).withSecond(59).withNano(999999999)

        return Pair(
            startOfMonth.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endOfMonth.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }

    /**
     * Formats a timestamp into a human-readable relative date string
     * @param timestamp The timestamp to format
     * @param skipMinutesAndHours Whether to skip minute and hour-based formatting
     * @return Formatted date string
     */
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