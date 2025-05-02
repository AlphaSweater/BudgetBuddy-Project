package com.synaptix.budgetbuddy.data.local.mapper

import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity


// -----------------------------
// Mappers from Entities
// AI assisted with creating the mappers
// -----------------------------

//fun BudgetEntity.toDomain() = Budget(
//    id = budget_id,
//    name = name,
//    minAmount = minAmount,
//    maxAmount = maxAmount
//)
//
//fun CategoryEntity.toDomain() = Category(
//    id = category_id,
//    name = name,
//    colour = colour,
//    icon = icon,
//    type = type
//)
//
//fun LabelEntity.toDomain() = Label(
//    id = label_id,
//    name = name
//)
//
//fun TransactionEntity.toDomain() = Transaction(
//    id = transaction_id,
//    amount = amount,
//    date = date,
//    note = note,
//    currency = currency,
//    label = label,
//    image = image,
//    recurrence = recurrence
//)
//
//fun WalletEntity.toDomain() = Wallet(
//    id = wallet_id,
//    name = name,
//    currency = currency,
//    balance = balance
//)
//
//fun UserEntity.toDomain() = User(
//    id = user_id,
//    firstName = firstName,
//    lastName = lastName,
//    email = email
//)
//AI assisted with the logic behind mapper
fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        transaction_id = this.transactionId ?: 0, // 0 for autoGenerate
        user_id = this.userId,
        wallet_id = this.walletId,
        category_id = this.categoryId,
        amount = this.amount,
        date = this.date,
        note = this.note ?: "",
        currency = this.currencyType,
        label = this.selectedLabels.joinToString(",") { it.labelName },
        image = this.photo?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) } ?: "",
        recurrence = this.recurrenceRate ?: ""
    )
}


fun Wallet.toEntity(): WalletEntity {
    return WalletEntity(
        wallet_id = this.walletId?: 0, // 0 for autoGenerate
        user_id = this.userId,
        name = this.walletName,
        currency = this.walletCurrency,
        balance = this.walletBalance
    )
}