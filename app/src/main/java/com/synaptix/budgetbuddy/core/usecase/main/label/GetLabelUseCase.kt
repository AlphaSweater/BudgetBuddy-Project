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

package com.synaptix.budgetbuddy.core.usecase.main.label

import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreLabelRepository
import com.synaptix.budgetbuddy.core.usecase.main.label.GetLabelUseCase.GetLabelsResult.*
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// UseCase class for retrieving labels associated with a user
class GetLabelUseCase @Inject constructor(
    // Injecting the LabelRepository to handle label-related operations
    private val labelRepository: FirestoreLabelRepository,
    private val userRepository: FirestoreUserRepository
) {
    sealed class GetLabelsResult {
        data class Success(val labels: List<Label>) : GetLabelsResult()
        data class Error(val message: String) : GetLabelsResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to fetch labels for the specified user
    suspend fun execute(userId: String): GetLabelsResult {
        // Input validation
        if (userId.isEmpty()) {
            return Error("Invalid user ID")
        }

        return try {
            val userResult = userRepository.getUserProfile(userId).first()
            val user = when (userResult) {
                is Result.Success -> userResult.data?.toDomain()
                is Result.Error -> return Error("Failed to get user data: ${userResult.exception.message}")
            }

            // Attempt to retrieve labels from the repository
            val labelsResult = labelRepository.getLabelsForUser(userId).first()
            val labels = when (labelsResult) {
                is Result.Success -> labelsResult.data.map { it.toDomain(user) }
                is Result.Error -> return Error("Failed to get labels: ${labelsResult.exception.message}")
            }

            // Return the labels if successful
            println("Labels retrieved: $labels")
            Success(labels)
        } catch (e: Exception) {
            println("Failed to get labels: ${e.message}")
            Error("Failed to get labels: ${e.message}")
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\