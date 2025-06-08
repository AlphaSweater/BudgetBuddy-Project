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

import android.util.Log
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreLabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

// UseCase class for adding a new label
class AddLabelUseCase @Inject constructor(
    // Injecting the FirestoreLabelRepository to handle label-related operations
    private val labelRepository: FirestoreLabelRepository
) {
    sealed class AddLabelResult {
        data class Success(val labelId: String) : AddLabelResult()
        data class Error(val message: String) : AddLabelResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to add a new label
    fun execute(newLabel: Label): Flow<AddLabelResult> = flow {
        try {
            // Input validation
            if (newLabel.name.isBlank()) {
                emit(AddLabelResult.Error("Label name cannot be empty"))
                return@flow
            }

            // Check if label name already exists for the user
            val nameExistsResult = labelRepository.labelNameExists(
                userId = newLabel.user?.id ?: "default",
                name = newLabel.name
            )

            when (nameExistsResult) {
                is Result.Success -> {
                    if (nameExistsResult.data) {
                        emit(AddLabelResult.Error("A label with this name already exists"))
                        return@flow
                    }
                }
                is Result.Error -> {
                    emit(AddLabelResult.Error("Failed to check label name: ${nameExistsResult.exception.message}"))
                    return@flow
                }
            }

            // Convert domain model to DTO using mapper
            val newLabelDTO = newLabel.toDTO()

            // Create the label
            when (val result = labelRepository.createLabel(
                userId = newLabel.user?.id ?: "default",
                label = newLabelDTO
            )) {
                is Result.Success -> {
                    Log.d("AddLabelUseCase", "Label added successfully: ${result.data}")
                    emit(AddLabelResult.Success(result.data))
                }
                is Result.Error -> {
                    Log.e("AddLabelUseCase", "Error adding label: ${result.exception.message}")
                    emit(AddLabelResult.Error("Failed to add label: ${result.exception.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e("AddLabelUseCase", "Exception while adding label: ${e.message}")
            emit(AddLabelResult.Error("Failed to add label: ${e.message}"))
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\