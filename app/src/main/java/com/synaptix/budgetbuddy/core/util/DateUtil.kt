package com.synaptix.budgetbuddy.core.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Utility class for common date range calculations
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
} 