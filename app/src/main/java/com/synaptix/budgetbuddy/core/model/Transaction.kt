//======================================================================================
// Group 2 - Group Members:
//======================================================================================
// * Chad Fairlie ST10269509
// * Dhiren Ruthenavelu ST10256859
// * Kayla Ferreira ST10259527
// * Nathan Teixeira ST10249266
//======================================================================================
// Declaration:
//======================================================================================
// We declare that this work is our own original work and that no part of it has been
// copied from any other source, except where explicitly acknowledged.
//======================================================================================
// References:
//======================================================================================
// * ChatGPT was used to help with the design and planning. As well as assisted with
//   finding and fixing errors in the code.
// * ChatGPT also helped with the forming of comments for the code.
// * https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.core.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

//======================================================================================
// Data Model: Transaction
// Represents the full transaction entity including related user, wallet, and category + label objects.
//======================================================================================
data class Transaction(
    override val id: String,
    val user: User,
    val wallet: Wallet,
    val category: Category,
    val labels: List<Label> = emptyList(),
    val amount: Double,
    val currency: String = "ZAR",
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val photoUrl: String? = null,
    val recurrenceData: RecurrenceData = RecurrenceData.DEFAULT,
) : Entity {
    companion object {
        fun new(
            user: User,
            wallet: Wallet,
            category: Category,
            labels: List<Label> = emptyList(),
            amount: Double,
            currency: String = "ZAR",
            date: Long = System.currentTimeMillis(),
            note: String = "",
            photoUrl: String? = null,
            recurrenceData: RecurrenceData = RecurrenceData.DEFAULT,
        ): Transaction = Transaction(
            id = "",
            user = user,
            wallet = wallet,
            category = category,
            labels = labels,
            amount = amount,
            currency = currency,
            date = date,
            note = note,
            photoUrl = photoUrl,
            recurrenceData = recurrenceData
        )
    }
}

//======================================================================================
// Data Model: RecurrenceData
// Represents the recurrence rules for a transaction, including type, interval, and end conditions.
//======================================================================================
data class RecurrenceData(
    val type: String,
    val interval: Int,
    val weekDays: List<String> = emptyList(),
    val isDayOfWeek: Boolean = false,
    val endType: String,
    val endValue: String? = null,
    var occurrenceCount: Int = 0 // tracks how many times it has occurred
) {
    companion object {
        val DEFAULT = RecurrenceData(
            type = "Once Off",
            interval = 1,
            endType = "Never"
        )
    }

    fun incrementOccurrenceCount() {
        occurrenceCount++
    }

    /**
     * Converts the recurrence data to a user-friendly display string.
     * Example: "Repeats every 2 weeks on Monday, Wednesday"
     */
    fun toDisplayString(): String {
        if (type == "Once Off") return "One-time transaction"

        val intervalStr = if (interval > 1) "every $interval " else ""

        val typeStr = when (type) {
            "Daily" -> if (interval == 1) "daily" else "days"
            "Weekly" -> if (interval == 1) "weekly" else "weeks"
            "Monthly" -> if (interval == 1) "monthly" else "months"
            "Yearly" -> if (interval == 1) "yearly" else "years"
            else -> ""
        }

        val weekDaysStr = if (weekDays.isNotEmpty()) " on ${weekDays.joinToString(", ")}" else ""
        val monthlyTypeStr = if (type == "Monthly" && isDayOfWeek) " on the same day of week" else ""

        val endStr = when (endType) {
            "After" -> " for $endValue occurrences"
            "On" -> " until $endValue"
            else -> ""
        }

        return "Repeats $intervalStr$typeStr$weekDaysStr$monthlyTypeStr$endStr"
    }

    fun isRecurring(): Boolean = type != "Once Off"

    /**
     * Calculates the next occurrence date based on the current date and recurrence pattern.
     * Returns null if the recurrence has ended.
     */
    fun getNextOccurrence(currentDate: Long): Long? {
        if (!isRecurring()) return currentDate

        val current = Instant.ofEpochMilli(currentDate).atZone(ZoneId.systemDefault()).toLocalDate()

        if (hasEnded(current)) return null

        val nextDate = when (type) {
            "Daily" -> current.plusDays(interval.toLong())
            "Weekly" -> calculateNextWeekly(current)
            "Monthly" -> calculateNextMonthly(current)
            "Yearly" -> current.plusYears(interval.toLong())
            else -> current
        }

        occurrenceCount++ // increment count only when next occurrence is requested

        return nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    //======================================================================================
    // Helper functions to determine the next occurrence date based on the recurrence type
    //======================================================================================

    private fun hasEnded(currentDate: LocalDate): Boolean {
        return when (endType) {
            "After" -> {
                val maxOccurrences = endValue?.toIntOrNull()
                maxOccurrences != null && occurrenceCount >= maxOccurrences
            }
            "On" -> {
                val endDate = parseEndDate()
                endDate != null && currentDate.isAfter(endDate)
            }
            else -> false
        }
    }

    private fun parseEndDate(): LocalDate? {
        return try {
            LocalDate.parse(endValue)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateNextWeekly(current: LocalDate): LocalDate {
        if (weekDays.isEmpty()) return current.plusWeeks(interval.toLong())

        val sortedDays = weekDays.mapNotNull { dayNameToDayOfWeek(it) }.sortedBy { it.value }
        val today = current.dayOfWeek
        val nextDay = sortedDays.firstOrNull { it.value > today.value } ?: sortedDays.first()
        val daysToAdd = (nextDay.value - today.value + 7) % 7

        return if (daysToAdd == 0) current.plusWeeks(interval.toLong()) else current.plusDays(daysToAdd.toLong())
    }

    private fun calculateNextMonthly(current: LocalDate): LocalDate {
        val targetMonth = current.plusMonths(interval.toLong())
        return if (isDayOfWeek) {
            val weekInMonth = (current.dayOfMonth - 1) / 7 + 1
            targetMonth.with(TemporalAdjusters.dayOfWeekInMonth(weekInMonth, current.dayOfWeek))
        } else {
            val day = current.dayOfMonth.coerceAtMost(targetMonth.lengthOfMonth())
            targetMonth.withDayOfMonth(day)
        }
    }

    private fun dayNameToDayOfWeek(name: String): DayOfWeek? {
        return try {
            DayOfWeek.valueOf(name.trim().uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}