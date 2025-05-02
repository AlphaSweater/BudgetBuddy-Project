// ======================================================================================
// Group 2 - Group Members:
// ======================================================================================
// * Chad Fairlie ST10269509
// * Dhiren Ruthenavelu ST10256859
// * Kayla Ferreira ST10259527
// * Nathan Teixeira ST10249266
// ======================================================================================
// Declaration:
// ======================================================================================
// We declare that this work is our own original work and that no part of it has been
// copied from any other source, except where explicitly acknowledged.
// ======================================================================================
// References:
// ======================================================================================
// * ChatGPT was used to help with the design and planning. As well as assisted with
//   finding and fixing errors in the code.
// * ChatGPT also helped with the forming of comments for the code.
// * https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
// ======================================================================================

package com.synaptix.budgetbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.synaptix.budgetbuddy.data.entity.LabelEntity


@Dao
interface LabelDao {

    // Inserts a new label into the database. If the label already exists, it ignores it.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(label: LabelEntity): Long

    //sql query to grab a label based on label ID
    @androidx.room.Query("SELECT * FROM label_table WHERE label_id = :labelId")
    suspend fun getLabelById(labelId: Int): LabelEntity?

    //sql query to grab all labels for a specific user
    @androidx.room.Query("SELECT * FROM label_table WHERE user_id = :userId OR user_id IS null")
    suspend fun getLabelsForUser(userId: Int): List<LabelEntity>
}