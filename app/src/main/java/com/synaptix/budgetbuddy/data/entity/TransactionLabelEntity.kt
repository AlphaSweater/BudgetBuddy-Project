package com.synaptix.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "transaction_label_table",
    primaryKeys = ["transaction_id", "label_id"],

)
data class TransactionLabelEntity (
    val transaction_id: Int = 0,
    val label_id: Int = 0
)