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

import com.synaptix.budgetbuddy.core.model.BudgetIn
import com.synaptix.budgetbuddy.data.entity.mapper.toEntity
import com.synaptix.budgetbuddy.data.repository.BudgetRepository
import javax.inject.Inject

// UseCase class responsible for adding a new budget
class AddBudgetUseCase @Inject constructor(
    // Injecting the BudgetRepository to handle budget-related operations
    private val budgetRepository: BudgetRepository
) {
    // Executes the operation to add a new budget by converting to entity and inserting into the database
    suspend fun execute(budget: BudgetIn) {
        // Convert the BudgetIn data model to the entity representation for database insertion
        val entity = budget.toEntity()

        // Insert the converted budget entity into the repository (database)
        budgetRepository.insertBudget(entity)
    }
}
