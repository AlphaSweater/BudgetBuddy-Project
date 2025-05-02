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
//    val selectedLabels: List<Label> = mutableListOf(), WIP
    val photo: ByteArray?,
    val recurrenceRate: String?
) : Serializable

//AI assisted with the creation of this data class
data class Transaction(
    val transactionId: Int,
    val user: User?,
    val wallet: Wallet?,
    val category: Category?,
    val currencyType: String,
    val amount: Double,
    val date: String,
    val note: String?,
//    val label: String,
    val photo: ByteArray?,
    val recurrenceRate: String?
)
