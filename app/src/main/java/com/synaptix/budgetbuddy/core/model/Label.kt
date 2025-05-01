package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

data class Label(
    val labelName: String,
    val transactionInfo: String,
    var isSelected: Boolean = false
) : Serializable
