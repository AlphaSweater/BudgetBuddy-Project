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
import com.synaptix.budgetbuddy.core.model.BudgetIn
import com.synaptix.budgetbuddy.core.usecase.main.budget.AddBudgetUseCase
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.model.WalletIn
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- ViewModel for Adding Budget ---
@HiltViewModel
class BudgetAddViewModel @Inject constructor(
    private val addBudgetUseCase: AddBudgetUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    // LiveData for budget input fields
    val budgetName = MutableLiveData<String?>()
    val wallet = MutableLiveData<Wallet?>()
    val category = MutableLiveData<Category?>()
    val budgetAmount = MutableLiveData<Double?>()

    // --- Function to Add Budget ---
    suspend fun addBudget() {
        val budget = BudgetIn(
            userId = getUserIdUseCase.execute(),
            budgetName = budgetName.value ?: "",
            walletId = wallet.value?.walletId ?: 0,
            categoryId = category.value?.categoryId ?: 0,
            amount = budgetAmount.value ?: 0.0,
            spent = 00.0
        )
        // Executes the use case to add the budget
        addBudgetUseCase.execute(budget)
    }

    // --- Function to Reset Input Fields ---
    fun reset() {
        budgetName.value = null
        wallet.value = null
        category.value = null
        budgetAmount.value = null
    }
}
