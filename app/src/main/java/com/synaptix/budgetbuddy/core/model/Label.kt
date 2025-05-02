package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

data class Label(
    val labelId: Int? = null,
    val labelName: String,
    val transactionInfo: String? = null,
    var isSelected: Boolean = false
) : Serializable
