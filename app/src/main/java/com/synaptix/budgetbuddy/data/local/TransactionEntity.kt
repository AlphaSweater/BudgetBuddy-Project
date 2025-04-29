package com.synaptix.budgetbuddy.data.local

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
        )
    ]
)

data class TransactionEntity (
    @PrimaryKey(autoGenerate = true) val transaction_id: Int,
    val user_id: Int,
    val wallet_id: Int
)