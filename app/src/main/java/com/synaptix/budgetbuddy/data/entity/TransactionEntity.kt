package com.synaptix.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(
    tableName = "transaction_table",
    //AI assisted with the creation of this foreign key
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"]
        ),
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["wallet_id"],
            childColumns = ["wallet_id"]
        ),
        ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["category_id"],
        childColumns = ["category_id"]
)
    ]
)

data class TransactionEntity (
    @PrimaryKey(autoGenerate = true) val transaction_id: Int,
    val user_id: Int,
    val wallet_id: Int,
    val category_id: Int,
    val amount: Double,
    val date: String,
    val note: String?,
    val currency: String,
    val image: ByteArray?,
    val recurrence: String?

)