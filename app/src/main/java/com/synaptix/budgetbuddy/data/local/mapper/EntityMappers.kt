package com.synaptix.budgetbuddy.data.local.mapper

import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity


//AI assisted with the logic behind mapper
fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        transaction_id = this.transactionId ?: 0,
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
fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        category_id = this.categoryId,
        user_id = this.userId,
        name = this.categoryName,
        colour = this.categoryColor,
        icon = this.categoryIcon,
        type = this.categoryType
    )
}

fun Wallet.toEntity(): WalletEntity {
    return WalletEntity(
        wallet_id = this.walletId,
        user_id = this.userId,
        name = this.walletName,
        currency = this.walletCurrency,
        balance = this.walletBalance
    )
}