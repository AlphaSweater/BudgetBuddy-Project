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
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreLabelRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// UseCase class for retrieving labels associated with a user
class GetLabelsUseCase @Inject constructor(
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
    fun execute(userId: String): Flow<GetLabelsResult> {
        Log.d("GetLabelsUseCase", "Starting to fetch labels for user: $userId")
        
        // Input validation
        if (userId.isEmpty()) {
            Log.e("GetLabelsUseCase", "Invalid user ID")
            return kotlinx.coroutines.flow.flow { 
                emit(GetLabelsResult.Error("Invalid user ID")) 
            }
        }

        return combine(
            userRepository.observeUserProfile(userId),
            labelRepository.observeLabelsForUser(userId)
        ) { user, labels ->
            Log.d("GetLabelsUseCase", "Received data - User: ${user != null}, Labels count: ${labels.size}")
            
            if (user == null) {
                Log.e("GetLabelsUseCase", "User not found")
                GetLabelsResult.Error("User not found")
            } else {
                val domainUser = user.toDomain()
                val domainLabels = labels.map { it.toDomain(domainUser) }
                Log.d("GetLabelsUseCase", "Successfully mapped ${domainLabels.size} labels")
                GetLabelsResult.Success(domainLabels)
            }
        }.flowOn(Dispatchers.IO)
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\