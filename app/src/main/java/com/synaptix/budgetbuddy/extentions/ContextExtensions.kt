package com.synaptix.budgetbuddy.extentions

import android.content.Context
import android.util.TypedValue

fun Context.getThemeColor(attrRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue.data
}
