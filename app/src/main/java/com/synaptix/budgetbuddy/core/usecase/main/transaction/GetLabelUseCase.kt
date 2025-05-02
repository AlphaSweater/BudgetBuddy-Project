//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.core.usecase.main.transaction

import com.synaptix.budgetbuddy.data.entity.LabelEntity
import com.synaptix.budgetbuddy.data.repository.LabelRepository
import javax.inject.Inject

// UseCase class for retrieving labels associated with a user
class GetLabelUseCase @Inject constructor(
    // Injecting the LabelRepository to handle label-related operations
    private val labelRepository: LabelRepository
) {
    // Executes the operation to fetch labels for the specified user
    suspend fun execute(userId: Int): List<LabelEntity> {
        // Logging the userId for which labels are being fetched
        println("labels for userId: $userId")

        // Fetches labels associated with the user from the repository
        return labelRepository.getLabelsForUser(userId)
    }
}
