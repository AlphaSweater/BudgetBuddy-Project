package com.synaptix.budgetbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.synaptix.budgetbuddy.data.entity.MinMaxGoalEntity

@Dao
interface MinMaxGoalsDao {

    @Query("SELECT * FROM min_max_goal_table WHERE user_id = :userId ORDER BY minMaxGoalId DESC LIMIT 1")
    suspend fun getGoalsForUser(userId: Int): MinMaxGoalEntity?


    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertMinMaxGoal(minMaxGoal: MinMaxGoalEntity): Long

}