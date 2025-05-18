// ======================================================================================
// Group 2 - Group Members:
// ======================================================================================
// * Chad Fairlie ST10269509
// * Dhiren Ruthenavelu ST10256859
// * Kayla Ferreira ST10259527
// * Nathan Teixeira ST10249266
// ======================================================================================
// Declaration:
// ======================================================================================
// We declare that this work is our own original work and that no part of it has been
// copied from any other source, except where explicitly acknowledged.
// ======================================================================================
// References:
// ======================================================================================
// * ChatGPT was used to help with the design and planning. As well as assisted with
//   finding and fixing errors in the code.
// * ChatGPT also helped with the forming of comments for the code.
// * https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
// ======================================================================================

package com.synaptix.budgetbuddy.data.entity.mapper

import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.BudgetIn
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.CategoryIn
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.TransactionIn
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.model.WalletIn
import com.synaptix.budgetbuddy.data.entity.BudgetEntity
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import com.synaptix.budgetbuddy.data.entity.relations.BudgetWithDetail
import com.synaptix.budgetbuddy.data.entity.relations.CategoryWithUser
import com.synaptix.budgetbuddy.data.entity.relations.TransactionWithDetail
import com.synaptix.budgetbuddy.data.entity.relations.WalletWithUser

//
// ================================
// Transaction Mappers
// ================================
// Maps from Transaction input model to database entity
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
//        label = this.selectedLabels.joinToString(",") { it.labelName },
        image = photo,
        recurrence = this.recurrenceRate ?: ""
    )
}

// Maps from Transaction entity to domain model
fun TransactionEntity.toDomain(user: User?, wallet: Wallet?, category: Category?): Transaction {
    return Transaction(
        transactionId = transaction_id,
        user = user,
        wallet = wallet,
        category = category,
        currencyType = currency,
        amount = amount,
        date = date,
        note = note,
        photo = image,
        recurrenceRate = recurrence
    )
}

// Maps from TransactionWithDetail relation to domain model
fun TransactionWithDetail.toDomain(): Transaction {
    return transaction.toDomain(user?.toDomain(), wallet?.toDomain(user?.toDomain()), category?.toDomain(user?.toDomain()))
}

//
// ================================
// Category Mappers
// ================================
// Maps from Category input model to database entity
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

// Maps from Category entity to domain model
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

// Maps from CategoryWithUser relation to domain model
fun CategoryWithUser.toDomain(): Category {
    return category.toDomain(user?.toDomain())
}

//
// ================================
// User Mappers
// ================================
// Maps from User domain model to entity
fun User.toEntity(): UserEntity {
    return UserEntity(
        user_id = this.userId,
        firstName = null,
        lastName = null,
        email = this.email,
        password = this.password
    )
}

// Maps from User entity to domain model
fun UserEntity.toDomain(): User {
    return User(
        userId = user_id,
        firstName = null,
        lastName = null,
        email = email,
        password = password
    )
}

//
// ================================
// Wallet Mappers
// ================================
// Maps from Wallet input model to entity
fun WalletIn.toEntity(): WalletEntity {
    return WalletEntity(
        wallet_id = this.walletId,
        user_id = this.userId,
        name = this.walletName,
        currency = this.walletCurrency,
        balance = this.walletBalance,
        excludeFromTotal = this.excludeFromTotal
    )
}

// Maps from Wallet entity to domain model
fun WalletEntity.toDomain(user: User?): Wallet {
    return Wallet(
        walletId = wallet_id,
        user = user,
        walletName = name,
        walletCurrency = currency,
        walletBalance = balance,
        excludeFromTotal = excludeFromTotal
    )
}

// Maps from WalletWithUser relation to domain model
fun WalletWithUser.toDomain(): Wallet {
    return wallet.toDomain(user?.toDomain())
}

//
// ================================
// Budget Mappers
// ================================
// Maps from Budget input model to entity
fun BudgetIn.toEntity(): BudgetEntity {
    return BudgetEntity(
        budget_id = this.budgetId,
        user_id = this.userId,
        wallet_id = this.walletId,
        category_id = this.categoryId,
        name = this.budgetName,
        amount = this.amount,
        spent = this.spent
    )
}

fun BudgetEntity.toDomain(user: User?, wallet: Wallet?, category: Category?): Budget {
    return Budget(
        budgetId = budget_id,
        user = user,
        wallet = wallet,
        category = category,
        budgetName = name,
        amount = amount,
        spent = spent
    )
}

fun BudgetWithDetail.toDomain(): Budget {
    return budget.toDomain(user?.toDomain(), wallet?.toDomain(user?.toDomain()), category?.toDomain(user?.toDomain()))
}