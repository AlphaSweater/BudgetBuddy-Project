package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.BudgetDTO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreBudgetRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val categoryRepository: FirestoreCategoryRepository
) : BaseFirestoreRepository<BudgetDTO>(firestore) {
    
    override val collection = firestore.collection("users")
    override val subCollectionName = "budgets"

    override fun getType(): Class<BudgetDTO> = BudgetDTO::class.java

    // Create a new budget
    suspend fun createBudget(userId: String, budget: BudgetDTO): Result<String> {
        val newBudget = budget.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return create(newBudget, userId)
    }

    // Update an existing budget
    suspend fun updateBudget(userId: String, budget: BudgetDTO): Result<Unit> {
        val updatedBudget = budget.copy(updatedAt = System.currentTimeMillis())
        return update(userId, budget.id, updatedBudget)
    }

    // Delete a budget
    suspend fun deleteBudget(userId: String, budgetId: String): Result<Unit> = 
        delete(userId, budgetId)

    // Get a single budget by ID with its categories
    suspend fun getBudgetById(userId: String, budgetId: String): Result<BudgetDTO?> =
        getById(userId, budgetId)

    // Get all budgets for a user with their categories
    suspend fun getBudgetsForUser(userId: String): Result<List<BudgetDTO>> {
        return try {
            val snapshot = createBaseQuery(userId).get().await()
            val budgets = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(budgets)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get active budgets for a user with their categories
    suspend fun getActiveBudgetsForUser(userId: String): Result<List<BudgetDTO>> {
        return try {
            val snapshot = createBaseQuery(userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val budgets = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(budgets)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getBudgetsByPeriod(userId: String, startDate: Long, endDate: Long): Result<List<BudgetDTO>> {
        return try {
            val snapshot = createBaseQuery(userId)
                .whereGreaterThanOrEqualTo("startDate", startDate)
                .whereLessThanOrEqualTo("startDate", endDate)
                .get()
                .await()
            
            val budgets = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(budgets)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Update budget spent amount
    suspend fun updateBudgetSpent(userId: String, budgetId: String, amount: Double): Result<Unit> {
        return try {
            val budget = when (val result = getBudgetById(userId, budgetId)) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error(result.exception)
            }

            val updatedBudget = budget!!.copy(
                spent = amount,
                updatedAt = System.currentTimeMillis()
            )
            update(userId, budgetId, updatedBudget)
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
            val snapshot = createBaseQuery(userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val total = snapshot.documents
                .mapNotNull { it.toObject(getType()) }
                .sumOf { it.amount }

            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get total spent amount for a user
    suspend fun getTotalSpentAmountForUser(userId: String): Result<Double> {
        return try {
            val snapshot = createBaseQuery(userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val total = snapshot.documents
                .mapNotNull { it.toObject(getType()) }
                .sumOf { it.spent }

            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Real-time listeners
    fun observeBudgetsForUser(userId: String): Flow<List<BudgetDTO>> {
        return observeCollection(userId, createBaseQuery(userId))
    }

    fun observeActiveBudgetsForUser(userId: String): Flow<List<BudgetDTO>> {
        val query = createBaseQuery(userId)
            .whereEqualTo("isActive", true)
        return observeCollection(userId, query)
    }

    fun observeBudget(userId: String, budgetId: String): Flow<BudgetDTO?> {
        return observeDocument(userId, budgetId)
    }

    fun observeTotalBudgetAmount(userId: String): Flow<Double> {
        return observeCollection(userId, createBaseQuery(userId))
            .map { budgets ->
                budgets.filter { it.isActive }
                    .sumOf { it.amount }
            }
    }

    fun observeTotalSpentAmount(userId: String): Flow<Double> {
        return observeCollection(userId, createBaseQuery(userId))
            .map { budgets ->
                budgets.filter { it.isActive }
                    .sumOf { it.spent }
            }
    }
} 