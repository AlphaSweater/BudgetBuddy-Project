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

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.auth.ValidateUserTotalsUseCase
import com.synaptix.budgetbuddy.data.firebase.model.BudgetDTO
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- ViewModel for Adding Budget ---
@HiltViewModel
class BudgetAddViewModel @Inject constructor(
    private val firestoreBudgetRepository: FirestoreBudgetRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val validateUserTotalsUseCase: ValidateUserTotalsUseCase
) : ViewModel() {

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // LiveData for budget input fields
    val budgetName = MutableLiveData<String?>()
    val wallet = MutableLiveData<Wallet?>()
    val selectedCategories = MutableLiveData<List<Category>>(emptyList())
    val budgetAmount = MutableLiveData<Double?>()

    private val _error = MutableLiveData<String?>()
    val error: MutableLiveData<String?> = _error

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Function to Add Budget
    suspend fun addBudget() {
        try {
            val userId = getUserIdUseCase.execute()
            val budget = BudgetDTO(
                userId = userId,
                name = budgetName.value ?: "",
                amount = budgetAmount.value ?: 0.0,
                spent = 0.0,
                categoryIds = selectedCategories.value?.map { it.id } ?: emptyList(),
                startDate = System.currentTimeMillis()
            )

            when (val result = firestoreBudgetRepository.createBudget(budget)) {
                is com.synaptix.budgetbuddy.core.model.Result.Success -> {
                    // OK
//                    validateUserTotalsUseCase.execute(userId)
                }
                is com.synaptix.budgetbuddy.core.model.Result.Error -> {
                    _error.value = result.exception.message
                }
            }
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Function to Reset Input Fields
    fun reset() {
        budgetName.value = null
        wallet.value = null
        selectedCategories.value = emptyList()
        budgetAmount.value = null
        _error.value = null
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\