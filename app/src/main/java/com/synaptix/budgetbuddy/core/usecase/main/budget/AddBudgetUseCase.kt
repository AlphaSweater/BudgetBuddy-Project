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

package com.synaptix.budgetbuddy.core.usecase.main.budget

import android.util.Log
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

//// UseCase class responsible for adding a new budget
class AddBudgetUseCase @Inject constructor(
    private val budgetRepository: FirestoreBudgetRepository
) {
    sealed class AddBudgetResult {
        data class Success(val budgetId: String) : AddBudgetResult()
        data class Error(val message: String) : AddBudgetResult()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to add a new budget
    fun execute(newBudget: Budget): Flow<AddBudgetResult> = flow {
        try {
            val newBudgetDTO = newBudget.toDTO()

            // Attempt to create the budget
            when (val result = budgetRepository.createBudget(newBudget.user.id, newBudgetDTO)) {
                is Result.Success -> {
                    Log.d("AddBudgetUseCase", "Budget added successfully: ${result.data}")
                    emit(AddBudgetResult.Success(result.data))
                }
                is Result.Error -> {
                    Log.e("AddBudgetUseCase", "Error adding budget: ${result.exception.message}")
                    emit(AddBudgetResult.Error("Failed to add budget: ${result.exception.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e("AddBudgetUseCase", "Exception while adding budget: ${e.message}")
            emit(AddBudgetResult.Error("Failed to add budget: ${e.message}"))
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
