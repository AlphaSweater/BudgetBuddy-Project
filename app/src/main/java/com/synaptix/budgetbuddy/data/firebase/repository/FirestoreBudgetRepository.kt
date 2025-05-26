package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.BudgetDTO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreBudgetRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val categoryRepository: FirestoreCategoryRepository
) : BaseFirestoreRepository<BudgetDTO>(firestore) {
    
    override val collection = firestore.collection("budgets")

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
    fun getBudgetById(budgetId: String): Flow<Result<BudgetDTO?>> = getById(budgetId) { docRef ->
        callbackFlow {
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                    return@addSnapshotListener
                }
                val budget = snapshot?.toObject(BudgetDTO::class.java)
                trySend(Result.Success(budget))
            }
            awaitClose { listener.remove() }
        }
    }

    // Get all budgets for a user with their categories
    fun getBudgetsForUser(userId: String): Flow<Result<List<BudgetDTO>>> = callbackFlow {
        val query = collection.whereEqualTo("userId", userId)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val budgets = snapshot?.documents?.mapNotNull { 
                it.toObject(BudgetDTO::class.java)
            } ?: emptyList()
            trySend(Result.Success(budgets))
        }

        awaitClose { listener.remove() }
    }

    // Get active budgets for a user with their categories
    fun getActiveBudgetsForUser(userId: String): Flow<Result<List<BudgetDTO>>> = callbackFlow {
        val query = collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val budgets = snapshot?.documents?.mapNotNull { 
                it.toObject(BudgetDTO::class.java)
            } ?: emptyList()
            trySend(Result.Success(budgets))
        }

        awaitClose { listener.remove() }
    }

    fun getBudgetsByPeriod(userId: String, startDate: Long, endDate: Long): Flow<Result<List<BudgetDTO>>> = callbackFlow {
        val query = collection
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("startDate", startDate)
            .whereLessThanOrEqualTo("startDate", endDate)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val budgets = snapshot?.documents?.mapNotNull { 
                it.toObject(BudgetDTO::class.java)
            } ?: emptyList()
            trySend(Result.Success(budgets))
        }

        awaitClose { listener.remove() }
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