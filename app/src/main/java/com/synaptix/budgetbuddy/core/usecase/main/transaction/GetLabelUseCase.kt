package com.synaptix.budgetbuddy.core.usecase.main.transaction

import com.synaptix.budgetbuddy.data.entity.LabelEntity
import com.synaptix.budgetbuddy.data.repository.LabelRepository
import javax.inject.Inject

class GetLabelUseCase @Inject constructor(
    private val labelRepository: LabelRepository
) {
    suspend fun execute(userId: Int): List<LabelEntity> {
        println("labels for userId: $userId")
        return labelRepository.getLabelsForUser(userId)
    }
}