package com.synaptix.budgetbuddy.core.model

import java.io.Serializable

data class User (
    val userId: Int,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val password: String
) : Serializable