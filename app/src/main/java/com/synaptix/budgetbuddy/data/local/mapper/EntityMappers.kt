package com.synaptix.budgetbuddy.data.local.mapper

import com.synaptix.budgetbuddy.data.entity.BudgetEntity
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.LabelEntity
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity

data class Budget(
    val id: Int,
    val name: String,
    val minAmount: Double,
    val maxAmount: Double
)

data class Category(
    val id: Int,
    val name: String,
    val colour: String,
    val icon: String,
    val type: String
)

data class Label(
    val id: Int,
    val name: String
)

data class Transaction(
    val id: Int,
    val amount: Double,
    val date: String,
    val note: String,
    val currency: String,
    val label: String,
    val image: String,
    val recurrence: String
)

data class Wallet(
    val id: Int,
    val name: String,
    val currency: String,
    val balance: Double
)

data class User(
    val id: Int,
    val firstName: String?,
    val lastName: String?,
    val email: String
)

// -----------------------------
// Mappers from Entities
// AI assisted with creating the mappers
// -----------------------------

fun BudgetEntity.toDomain() = Budget(
    id = budget_id,
    name = name,
    minAmount = minAmount,
    maxAmount = maxAmount
)

fun CategoryEntity.toDomain() = Category(
    id = category_id,
    name = name,
    colour = colour,
    icon = icon,
    type = type
)

fun LabelEntity.toDomain() = Label(
    id = label_id,
    name = name
)

fun TransactionEntity.toDomain() = Transaction(
    id = transaction_id,
    amount = amount,
    date = date,
    note = note,
    currency = currency,
    label = label,
    image = image,
    recurrence = recurrence
)

fun WalletEntity.toDomain() = Wallet(
    id = wallet_id,
    name = name,
    currency = currency,
    balance = balance
)

fun UserEntity.toDomain() = User(
    id = user_id,
    firstName = firstName,
    lastName = lastName,
    email = email
)