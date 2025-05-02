package com.synaptix.budgetbuddy.core.model

import androidx.room.Embedded
import androidx.room.Relation
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import java.io.Serializable

data class TransactionIn(
    val transactionId: Int? = null,
    val userId: Int,
    val walletId: Int,
    val categoryId: Int,
    val currencyType: String,
    val amount: Double,
    val date: String,
    val note: String?,
    val selectedLabels: List<Label> = mutableListOf(),
    val photo: ByteArray?,
    val recurrenceRate: String?
) : Serializable

//AI assisted with the creation of this data class
data class Transaction(
    val id: Int,
    val amount: Double,
    val date: String,
    val note: String,
    val currency: String,
    val label: String,
    val image: String,
    val recurrence: String,

    val user: UserEntity,
    val wallet: WalletEntity,
    val category: CategoryEntity
)
data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val user: UserEntity,

    @Relation(
        parentColumn = "wallet_id",
        entityColumn = "wallet_id"
    )
    val wallet: WalletEntity,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "category_id"
    )
    val category: CategoryEntity
)

