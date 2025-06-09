package com.synaptix.budgetbuddy.core.util

import android.widget.ImageView
import android.widget.TextView
import com.synaptix.budgetbuddy.R

/**
 * Utility class for handling privacy-related functionality
 */
object PrivacyUtil {
    /**
     * Toggles the visibility of a balance display with an eye icon
     * 
     * @param isVisible Current visibility state
     * @param balanceView TextView to show/hide the balance
     * @param eyeIcon ImageView for the eye icon
     * @param balance Current balance value
     * @return New visibility state
     */
    fun toggleBalanceVisibility(
        isVisible: Boolean,
        balanceView: TextView,
        eyeIcon: ImageView,
        balance: Double?
    ): Boolean {
        val newVisibility = !isVisible
        
        // Update eye icon
        eyeIcon.setImageResource(
            if (newVisibility) R.drawable.ic_ui_privacy_eye_open
            else R.drawable.ic_ui_privacy_eye_closed
        )

        // Update balance text
        balanceView.text = if (newVisibility) {
            CurrencyUtil.formatWithoutSymbol(balance)
        } else {
            "••••••"
        }

        return newVisibility
    }
} 