package com.synaptix.budgetbuddy.data.entity

import androidx.room.Entity


@Entity(
    tableName = "transaction_label_table",
    primaryKeys = ["transaction_id", "label_id"],

)
class TransactionLabelEntity {
    val transaction_id: Int = 0
    val label_id: Int = 0
}