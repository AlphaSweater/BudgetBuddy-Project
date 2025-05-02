package com.synaptix.budgetbuddy.core.model

import androidx.room.Embedded
import androidx.room.Relation
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import java.io.Serializable

data class Transaction(
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
data class TransactionFull(
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
fun TransactionWithDetails.toTransactionFull(): TransactionFull {
    return TransactionFull(
        id = transaction.transaction_id,
        amount = transaction.amount,
        date = transaction.date,
        note = transaction.note,
        currency = transaction.currency,
        label = transaction.label,
        image = transaction.image,
        recurrence = transaction.recurrence,

        user = user,
        wallet = wallet,
        category = category
    )
}