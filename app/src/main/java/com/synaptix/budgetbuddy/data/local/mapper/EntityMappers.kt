package com.synaptix.budgetbuddy.data.local.mapper

import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.CategoryIn
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.TransactionIn
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import com.synaptix.budgetbuddy.data.entity.relations.CategoryWithUser


//AI assisted with the logic behind mapper
fun TransactionIn.toEntity(): TransactionEntity {
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

fun CategoryIn.toEntity(): CategoryEntity {
    return CategoryEntity(
        category_id = categoryId,
        user_id = userId,
        name = categoryName,
        type = categoryType,
        colour = categoryColor,
        icon = categoryIcon
    )
}

fun CategoryEntity.toDomain(user: User?): Category {
    return Category(
        categoryId = category_id,
        user = user,
        categoryName = name,
        categoryType = type,
        categoryIcon = icon,
        categoryColor = colour
    )
}

// Relation
fun CategoryWithUser.toDomain(): Category {
    return category.toDomain(user?.toDomain())
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        user_id = this.userId,
        firstName = null,
        lastName = null,
        email = this.email,
        password = this.password
    )
}

fun UserEntity.toDomain(): User {
    return User(
        userId = user_id,
        firstName = null,
        lastName = null,
        email = email,
        password = password
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