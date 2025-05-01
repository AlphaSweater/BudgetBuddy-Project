package com.synaptix.budgetbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.synaptix.budgetbuddy.data.entity.LabelEntity


@Dao
interface LabelDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(label: LabelEntity): Long

    //sql query to grab a label based on label ID
    @androidx.room.Query("SELECT * FROM label_table WHERE label_id = :labelId")
    suspend fun getLabelById(labelId: Int): LabelEntity?

    //sql query to grab all labels for a specific user
    @androidx.room.Query("SELECT * FROM label_table WHERE user_id = :userId OR user_id IS null")
    suspend fun getLabelsForUser(userId: Int): List<LabelEntity>
}