package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.data.entity.LabelEntity
import com.synaptix.budgetbuddy.data.local.dao.LabelDao

class LabelRepository(private val labelDao: LabelDao) {

    // Fetch all labels for a specific user
    suspend fun getLabelsForUser(userId: Int): List<LabelEntity> {
        return labelDao.getLabelsForUser(userId)
    }
}