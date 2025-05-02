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

package com.synaptix.budgetbuddy.core.usecase.auth

import com.synaptix.budgetbuddy.data.repository.UserRepository
import javax.inject.Inject

// UseCase class responsible for fetching the current user's ID from the repository
class GetUserIdUseCase @Inject constructor(
    // Injecting the UserRepository dependency to interact with user data
    private val userRepository: UserRepository
) {
    // Executes the operation to fetch the current user's ID
    // This is a suspend function, indicating it runs asynchronously
    suspend fun execute(): Int {
        // Fetches the current user ID from the UserRepository
        return userRepository.getCurrentUserId()
    }
}
