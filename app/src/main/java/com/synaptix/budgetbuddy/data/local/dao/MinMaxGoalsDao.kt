package com.synaptix.budgetbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.synaptix.budgetbuddy.data.entity.MinMaxGoalEntity

@Dao
interface MinMaxGoalsDao {

    @Query("SELECT * FROM min_max_goal_table WHERE user_id = :userId")
    suspend fun getGoalsForUser(userId: Int): MinMaxGoalEntity?

}