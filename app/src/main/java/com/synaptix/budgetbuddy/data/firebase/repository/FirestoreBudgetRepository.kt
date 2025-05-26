package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.BudgetDTO
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreBudgetRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val categoryRepository: FirestoreCategoryRepository
) : BaseFirestoreRepository<BudgetDTO>(firestore) {
    
    override val collection = firestore.collection("budgets")

    override fun getType(): Class<BudgetDTO> = BudgetDTO::class.java

    // Create a new budget
    suspend fun createBudget(budget: BudgetDTO): Result<String> {
        val newBudget = budget.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return create(newBudget)
    }

    // Update an existing budget
    suspend fun updateBudget(budget: BudgetDTO): Result<Unit> {
        val updatedBudget = budget.copy(updatedAt = System.currentTimeMillis())
        return update(budget.id, updatedBudget)
    }

    // Delete a budget
    suspend fun deleteBudget(budgetId: String): Result<Unit> = delete(budgetId)

    // Get a single budget by ID with its categories
    suspend fun getBudgetById(budgetId: String): Result<BudgetDTO?> {
        return try {
            val snapshot = collection.document(budgetId).get().await()
            Result.Success(snapshot.toObject(BudgetDTO::class.java))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get all budgets for a user with their categories
    suspend fun getBudgetsForUser(userId: String): Result<List<BudgetDTO>> {
        return try {
            val snapshot = collection.whereEqualTo("userId", userId).get().await()
            val budgets = snapshot.documents.mapNotNull { 
                it.toObject(BudgetDTO::class.java)
            }
            Result.Success(budgets)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get active budgets for a user with their categories
    suspend fun getActiveBudgetsForUser(userId: String): Result<List<BudgetDTO>> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val budgets = snapshot.documents.mapNotNull { 
                it.toObject(BudgetDTO::class.java)
            }
            Result.Success(budgets)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getBudgetsByPeriod(userId: String, startDate: Long, endDate: Long): Result<List<BudgetDTO>> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("startDate", startDate)
                .whereLessThanOrEqualTo("startDate", endDate)
                .get()
                .await()
            
            val budgets = snapshot.documents.mapNotNull { 
                it.toObject(BudgetDTO::class.java)
            }
            Result.Success(budgets)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Update budget spent amount
    suspend fun updateBudgetSpent(budgetId: String, amount: Double): Result<Unit> {
        return try {
            val budget = collection.document(budgetId).get().await()
                .toObject(BudgetDTO::class.java)
                ?: return Result.Error(Exception("Budget not found"))

            val updatedBudget = budget.copy(
                spent = amount,
                updatedAt = System.currentTimeMillis()
            )
            update(budgetId, updatedBudget)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Check if a budget name already exists for a user
    suspend fun budgetNameExists(userId: String, name: String): Result<Boolean> = 
        checkNameExists(userId, name)

    // Get total budget amount for a user
    suspend fun getTotalBudgetAmountForUser(userId: String): Result<Double> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val total = snapshot.documents
                .mapNotNull { it.toObject(BudgetDTO::class.java) }
                .filter { it.isActive }
                .sumOf { it.amount }

            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get total spent amount for a user
    suspend fun getTotalSpentAmountForUser(userId: String): Result<Double> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val total = snapshot.documents
                .mapNotNull { it.toObject(BudgetDTO::class.java) }
                .filter { it.isActive }
                .sumOf { it.spent }

            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 