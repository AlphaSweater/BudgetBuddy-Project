package com.synaptix.budgetbuddy.data.firebase.mapper

import com.synaptix.budgetbuddy.core.model.*
import com.synaptix.budgetbuddy.data.firebase.model.*

object FirebaseMapper {
    // Helper function to check if an entity is new
    private fun Entity.isNew() = id.isEmpty()

    // User mappings
    fun User.toDTO(): UserDTO = UserDTO(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        lastLoginAt = lastLoginAt
    )

    fun UserDTO.toDomain(): User = User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        lastLoginAt = lastLoginAt
    )

    // Wallet mappings
    fun Wallet.toDTO(): WalletDTO = WalletDTO(
        id = id,
        userId = user.id,
        name = name,
        currency = currency,
        balance = balance,
        excludeFromTotal = excludeFromTotal,
        lastTransactionAt = lastTransactionAt
    )

    fun WalletDTO.toDomain(user: User): Wallet = Wallet(
        id = id,
        user = user,
        name = name,
        currency = currency,
        balance = balance,
        excludeFromTotal = excludeFromTotal,
        lastTransactionAt = lastTransactionAt
    )

    // Category mappings
    fun Category.toDTO(): CategoryDTO = CategoryDTO(
        id = id,
        userId = user?.id,
        name = name,
        type = type,
        color = color,
        icon = icon,
        isDefault = user == null
    )

    fun CategoryDTO.toDomain(user: User?): Category = Category(
        id = id,
        user = user,
        name = name,
        type = type,
        icon = icon,
        color = color
    )

    // Label mappings
    fun Label.toDTO(): LabelDTO = LabelDTO(
        id = id,
        userId = user?.id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun LabelDTO.toDomain(user: User?): Label = Label(
        id = id,
        user = user,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    // RecurrenceData mappings
    fun RecurrenceData.toDTO(): RecurrenceDataDTO = RecurrenceDataDTO(
        type = type,
        interval = interval,
        weekDays = weekDays,
        isDayOfWeek = isDayOfWeek,
        endType = endType,
        endValue = endValue,
        occurrenceCount = occurrenceCount
    )

    fun RecurrenceDataDTO.toDomain(): RecurrenceData = RecurrenceData(
        type = type,
        interval = interval,
        weekDays = weekDays,
        isDayOfWeek = isDayOfWeek,
        endType = endType,
        endValue = endValue,
        occurrenceCount = occurrenceCount
    )

    // Transaction mappings
    fun Transaction.toDTO(): TransactionDTO = TransactionDTO(
        id = id,
        userId = user.id,
        walletId = wallet.id,
        categoryId = category.id,
        amount = amount,
        currency = currency,
        date = date,
        note = note,
        photoUrl = photoUrl,
        recurrenceData = recurrenceData.toDTO(),
        labelIds = labels.map { it.id }
    )

    fun TransactionDTO.toDomain(
        user: User,
        wallet: Wallet,
        category: Category,
        labels: List<Label>
    ): Transaction = Transaction(
        id = id,
        user = user,
        wallet = wallet,
        category = category,
        labels = labels,
        amount = amount,
        currency = currency,
        date = date,
        note = note,
        photoUrl = photoUrl,
        recurrenceData = recurrenceData.toDomain()
    )

    // Budget mappings
    fun Budget.toDTO(): BudgetDTO = BudgetDTO(
        id = id,
        userId = user.id,
        name = name,
        amount = amount,
        spent = spent,
        categoryIds = categories.map { it.id },
        startDate = startDate,
        endDate = endDate,
        isRecurring = isRecurring,
        recurrencePeriod = recurrencePeriod
    )

    fun BudgetDTO.toDomain(
        user: User,
        categories: List<Category>
    ): Budget = Budget(
        id = id,
        user = user,
        name = name,
        amount = amount,
        spent = spent,
        categories = categories,
        startDate = startDate,
        endDate = endDate,
        isRecurring = isRecurring,
        recurrencePeriod = recurrencePeriod
    )
}