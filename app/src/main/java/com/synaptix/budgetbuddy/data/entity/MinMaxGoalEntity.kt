package com.synaptix.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "min_max_goal_table",
        foreignKeys = [
            ForeignKey(
                entity = UserEntity::class,
                parentColumns = ["user_id"],
                childColumns = ["user_id"]
            )
        ]
    )

    class MinMaxGoalEntity {
    @PrimaryKey(autoGenerate = true) var minMaxGoalId: Int = 0
    var user_id: Int = 0
    var minGoal: Double = 0.0
    var maxGoal: Double = 0.0
}