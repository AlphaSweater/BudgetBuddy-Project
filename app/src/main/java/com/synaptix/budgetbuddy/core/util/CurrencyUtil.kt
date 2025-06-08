package com.synaptix.budgetbuddy.core.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import java.text.DecimalFormatSymbols

/**
 * Utility class for currency formatting operations
 */
object CurrencyUtil {
    private val decimalFormat = DecimalFormat("#,##0.00").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    }

    /**
     * Formats a number as currency with the Rand symbol (R)
     * @param amount The amount to format
     * @return Formatted string (e.g., "R 1,234.56")
     */
    fun formatWithSymbol(amount: Double?): String {
        if (amount == null) return "R 0.00"
        return "R ${decimalFormat.format(amount)}"
    }

    /**
     * Formats a number as currency without the Rand symbol
     * @param amount The amount to format
     * @return Formatted string (e.g., "1,234.56")
     */
    fun formatWithoutSymbol(amount: Double?): String {
        if (amount == null) return "0.00"
        return decimalFormat.format(amount)
    }
} 