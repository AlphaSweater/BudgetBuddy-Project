package com.synaptix.budgetbuddy.core.usecase.auth

import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet

import javax.inject.Inject
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreUserRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreBudgetRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreWalletRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreCategoryRepository
import com.synaptix.budgetbuddy.data.firebase.repository.FirestoreTransactionRepository
import com.synaptix.budgetbuddy.data.firebase.mapper.FirebaseMapper.toDomain
import kotlinx.coroutines.flow.first
import com.synaptix.budgetbuddy.core.model.Result

class ValidateUserTotalsUseCase @Inject constructor(
    private val budgetRepository : FirestoreBudgetRepository,
    private val userRepository : FirestoreUserRepository,
    private val walletRepository : FirestoreWalletRepository,
    private val categoryRepository : FirestoreCategoryRepository,
    private val transactionRepository : FirestoreTransactionRepository
){
    suspend fun execute(userId: String){
        // ensure userId is not null
        if (userId.isEmpty()){
            throw Exception("Invalid user ID")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch user profile based on provided userid
        val userResult = userRepository.getUserProfile(userId).first()
        val user = when (userResult) {
            is Result.Success -> userResult.data?.toDomain()
            is Result.Error -> throw Exception("Failed to get user data: ${userResult.exception.message}")
        }
        //checks to see if user object is not null
        if (user == null) {
            throw Exception("User not found")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch wallets based on user id
        val walletResult = walletRepository.getWalletsForUser(userId).first()
        val wallets = when (walletResult) {
            is Result.Success -> walletResult.data.map { it.toDomain(user) }
            is Result.Error -> throw Exception("Failed to get wallets: ${walletResult.exception.message}")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch categories based on user id
        val categorieResult = categoryRepository.getCategoriesForUser(userId).first()
        val categories = when (categorieResult) {
            is Result.Success -> categorieResult.data.map { it.toDomain(user) }
            is Result.Error -> throw Exception("Failed to get categories: ${categorieResult.exception.message}")
        }

        val labels = emptyList<Label>()

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch budgets for the user
        val budgetResult = budgetRepository.getBudgetsForUser(userId).first()
        val budgets = when (budgetResult) {
            is Result.Success -> budgetResult.data.map { it.toDomain(user, categories) }
            is Result.Error -> throw Exception("Failed to get budgets: ${budgetResult.exception.message}")
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
        // Fetch transactions for the user
        val transactionResult = transactionRepository.getTransactionsForUser(userId).first()
        val transactions = when (transactionResult) {
            is Result.Success -> transactionResult.data.map { dto ->
                val wallet = wallets.find { it.id == dto.walletId }
                    ?: throw Exception("Wallet with ID ${dto.walletId} not found for transaction ${dto.id}")

                val category = categories.find { it.id == dto.categoryId }
                    ?: throw Exception("Category with ID ${dto.categoryId} not found for transaction ${dto.id}")

                val labels = emptyList<Label>()

                dto.toDomain(user, wallet, category, labels)
            }

            is Result.Error -> throw Exception("Failed to get transactions: ${transactionResult.exception.message}")
        }

        calculateWalletTotals(transactions, wallets)
        calculateBudgetTotals(transactions, budgets)

    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Function to calculate wallet totals based on transactions
    suspend fun calculateWalletTotals(
        transactions: List<Transaction>,
        wallets: List<Wallet>,
    ) {
        wallets.forEach { wallet ->
            val walletTransactions = transactions.filter { it.wallet.id == wallet.id }

            // Calculate totals based on income and expense
            val incomeTotal = walletTransactions
                .filter { it.category.type == "income" }
                .sumOf { it.amount }

            val expenseTotal = walletTransactions
                .filter { it.category.type == "expense" }
                .sumOf { it.amount }

            val newBalance = incomeTotal - expenseTotal

            // Update the wallet in Firestore
            val updateResult = walletRepository.updateWalletBalance(wallet.id, newBalance)
            if (updateResult is Result.Error) {
                throw Exception("Failed to update wallet ${wallet.name}: ${updateResult.exception.message}")
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Function that calculates amount spent for each budget based on transactions and categories
    suspend fun calculateBudgetTotals(
        transactions: List<Transaction>,
        budgets: List<Budget>
    ) {
        budgets.forEach { budget ->
            // Get all category IDs for this budget
            val budgetCategoryIds = budget.categories.map { it.id }

            // Filter expense transactions with matching category ID
            val matchingTransactions = transactions.filter {
                it.category.type == "expense" &&
                        budgetCategoryIds.contains(it.category.id)
            }

            val totalSpent = matchingTransactions.sumOf { it.amount }

            // Update in Firestore
            val result = budgetRepository.updateBudgetSpent(budget.id, totalSpent)
            if (result is Result.Error) {
                throw Exception("Failed to update budget ${budget.name}: ${result.exception.message}")
            }
        }
    }

}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\