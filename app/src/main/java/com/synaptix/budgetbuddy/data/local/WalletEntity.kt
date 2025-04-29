package com.synaptix.budgetbuddy.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.synaptix.budgetbuddy.data.entity.UserEntity

@Entity(
    tableName = "wallet_table",
    //AI assisted with the creation of this foreign key
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"]
        )
    ]
)
data class WalletEntity (
    @PrimaryKey(autoGenerate = true) val wallet_id: Int,
    val user_id: Int,
    val name: String
)