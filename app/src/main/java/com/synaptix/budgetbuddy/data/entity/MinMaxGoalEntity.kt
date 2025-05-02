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

class MinMaxGoalEntity (
    @PrimaryKey(autoGenerate = true) var minMaxGoalId: Int,
    var user_id: Int,
    var minGoal: Double,
    var maxGoal: Double
)