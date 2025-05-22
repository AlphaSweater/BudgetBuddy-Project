package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.TransactionDTO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreTransactionRepository @Inject constructor(
    firestore: FirebaseFirestore
) : BaseFirestoreRepository<TransactionDTO>(firestore) {
    
    override val collection = firestore.collection("transactions")

    // Create a new transaction
    suspend fun createTransaction(transaction: TransactionDTO): Result<String> {
        val newTransaction = transaction.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return create(newTransaction)
    }

    // Update an existing transaction
    suspend fun updateTransaction(transaction: TransactionDTO): Result<Unit> {
        val updatedTransaction = transaction.copy(updatedAt = System.currentTimeMillis())
        return update(transaction.id, updatedTransaction)
    }

    // Delete a transaction
    suspend fun deleteTransaction(transactionId: String): Result<Unit> = delete(transactionId)

    // Get a single transaction by ID
    fun getTransactionById(transactionId: String): Flow<Result<TransactionDTO?>> = getById(transactionId) { docRef ->
        callbackFlow {
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                    return@addSnapshotListener
                }
                val transaction = snapshot?.toObject(TransactionDTO::class.java)

                trySend(Result.Success(transaction))
            }
            awaitClose { listener.remove() }
        }
    }

    // Get all transactions for a user with their labels
    fun getTransactionsForUser(userId: String): Flow<Result<List<TransactionDTO>>> = getAll(createBaseQueryWithUserId(userId)) { query ->
        callbackFlow {
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { 
                    it.toObject(TransactionDTO::class.java)
                } ?: emptyList()

                trySend(Result.Success(transactions))
            }
            awaitClose { listener.remove() }
        }
    }



    // Get transactions for a specific wallet with their labels
    fun getTransactionsForWallet(walletId: String): Flow<Result<List<TransactionDTO>>> = callbackFlow {
        val query = collection.whereEqualTo("walletId", walletId)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            } ?: emptyList()

            trySend(Result.Success(transactions))
        }

        awaitClose { listener.remove() }
    }

    // Get transactions for a specific category with their labels
    fun getTransactionsForCategory(categoryId: String): Flow<Result<List<TransactionDTO>>> = callbackFlow {
        val query = collection.whereEqualTo("categoryId", categoryId)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            } ?: emptyList()

            trySend(Result.Success(transactions))
        }

        awaitClose { listener.remove() }
    }

    // Get transactions within a date range with their labels
    suspend fun getTransactionsInDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQueryWithUserId(userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            }

            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get total amount for a user in a date range
    suspend fun getTotalAmountForUserInDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Result<Double> {
        return try {
            val transactions = getTransactionsInDateRange(userId, startDate, endDate)
            when (transactions) {
                is Result.Success -> {
                    val total = transactions.data.sumOf { it.amount }
                    Result.Success(total)
                }
                is Result.Error -> Result.Error(transactions.exception)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions for a specific user within a date range with their labels
    fun getTransactionsForUserInDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<Result<List<TransactionDTO>>> = callbackFlow {
        val query = createBaseQueryWithUserId(userId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            } ?: emptyList()

            trySend(Result.Success(transactions))
        }

        awaitClose { listener.remove() }
    }

    // Get transactions for a specific wallet within a date range with their labels
    fun getTransactionsForWalletInDateRange(
        walletId: String,
        startDate: Long,
        endDate: Long
    ): Flow<Result<List<TransactionDTO>>> = callbackFlow {
        val query = collection
            .whereEqualTo("walletId", walletId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            } ?: emptyList()

            trySend(Result.Success(transactions))
        }

        awaitClose { listener.remove() }
    }

    // Get transactions for a specific category within a date range with their labels
    fun getTransactionsForCategoryInDateRange(
        categoryId: String,
        startDate: Long,
        endDate: Long
    ): Flow<Result<List<TransactionDTO>>> = callbackFlow {
        val query = collection
            .whereEqualTo("categoryId", categoryId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            } ?: emptyList()

            trySend(Result.Success(transactions))
        }

        awaitClose { listener.remove() }
    }

    // Get total amount for a wallet in a date range
    suspend fun getTotalAmountForWalletInDateRange(
        walletId: String,
        startDate: Long,
        endDate: Long
    ): Result<Double> {
        return try {
            val snapshot = collection
                .whereEqualTo("walletId", walletId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .await()

            val total = snapshot.documents.sumOf { 
                it.toObject(TransactionDTO::class.java)?.amount ?: 0.0
            }
            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get total amount for a category in a date range
    suspend fun getTotalAmountForCategoryInDateRange(
        categoryId: String,
        startDate: Long,
        endDate: Long
    ): Result<Double> {
        return try {
            val snapshot = collection
                .whereEqualTo("categoryId", categoryId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .await()

            val total = snapshot.documents.sumOf { 
                it.toObject(TransactionDTO::class.java)?.amount ?: 0.0
            }
            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions by label IDs
    fun getTransactionsByLabels(labelIds: List<String>): Flow<Result<List<TransactionDTO>>> = callbackFlow {
        val query = collection.whereArrayContainsAny("labelIds", labelIds)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            } ?: emptyList()

            trySend(Result.Success(transactions))
        }

        awaitClose { listener.remove() }
    }

    // Get transactions by search term (searches in note field)
    fun searchTransactions(searchTerm: String): Flow<Result<List<TransactionDTO>>> = callbackFlow {
        val query = collection
            .orderBy("note")
            .startAt(searchTerm)
            .endAt(searchTerm + "\uf8ff")
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            } ?: emptyList()

            trySend(Result.Success(transactions))
        }

        awaitClose { listener.remove() }
    }

    // Get recurring transactions
    fun getRecurringTransactions(): Flow<Result<List<TransactionDTO>>> = callbackFlow {
        val query = collection.whereEqualTo("isRecurring", true)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            } ?: emptyList()

            trySend(Result.Success(transactions))
        }

        awaitClose { listener.remove() }
    }

    // Get transactions that need to be processed (for recurring transactions)
    suspend fun getTransactionsToProcess(): Result<List<TransactionDTO>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val snapshot = collection
                .whereEqualTo("isRecurring", true)
                .whereLessThanOrEqualTo("nextOccurrence", currentTime)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            }

            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 