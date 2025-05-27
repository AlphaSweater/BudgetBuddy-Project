package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.LabelDTO
import com.synaptix.budgetbuddy.data.firebase.model.TransactionDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreTransactionRepository @Inject constructor(
    private val firestoreInstance: FirebaseFirestore
) : BaseFirestoreRepository<TransactionDTO>(firestoreInstance) {
    
    override val collection = firestoreInstance.collection("users")
    override val subCollectionName = "transactions"

    override fun getType(): Class<TransactionDTO> = TransactionDTO::class.java

    // Create a new transaction
    suspend fun createTransaction(userId: String, transaction: TransactionDTO): Result<String> {
        val newTransaction = transaction.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return create(newTransaction, userId)
    }

    // Update an existing transaction
    suspend fun updateTransaction(userId: String, transaction: TransactionDTO): Result<Unit> {
        val updatedTransaction = transaction.copy(updatedAt = System.currentTimeMillis())
        return update(userId, transaction.id, updatedTransaction)
    }

    // Delete a transaction
    suspend fun deleteTransaction(userId: String, transactionId: String): Result<Unit> = 
        delete(userId, transactionId)

    // Get a single transaction by ID
    suspend fun getTransactionById(userId: String, transactionId: String): Result<TransactionDTO?> =
        getById(userId, transactionId)

    // Get all transactions for a user
    suspend fun getTransactionsForUser(userId: String): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQuery(userId).get().await()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions for a specific wallet
    suspend fun getTransactionsForWallet(userId: String, walletId: String): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQuery(userId)
                .whereEqualTo("walletId", walletId)
                .get()
                .await()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions for a specific category
    suspend fun getTransactionsForCategory(userId: String, categoryId: String): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQuery(userId)
                .whereEqualTo("categoryId", categoryId)
                .get()
                .await()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions within a date range
    suspend fun getTransactionsInDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQuery(userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(getType())
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
    ): Result<Double> = try {
        val snapshot = createBaseQuery(userId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .await()

        val total = snapshot.documents.sumOf { 
            it.toObject(getType())?.amount ?: 0.0
        }
        Result.Success(total)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Get transactions for a specific user within a date range
    suspend fun getTransactionsForUserInDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQuery(userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions for a specific wallet within a date range
    suspend fun getTransactionsForWalletInDateRange(
        userId: String,
        walletId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQuery(userId)
                .whereEqualTo("walletId", walletId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions for a specific category within a date range
    suspend fun getTransactionsForCategoryInDateRange(
        userId: String,
        categoryId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQuery(userId)
                .whereEqualTo("categoryId", categoryId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get total amount for a wallet in a date range
    suspend fun getTotalAmountForWalletInDateRange(
        userId: String,
        walletId: String,
        startDate: Long,
        endDate: Long
    ): Result<Double> = try {
        val snapshot = createBaseQuery(userId)
            .whereEqualTo("walletId", walletId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .await()

        val total = snapshot.documents.sumOf { 
            it.toObject(getType())?.amount ?: 0.0
        }
        Result.Success(total)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Get total amount for a category in a date range
    suspend fun getTotalAmountForCategoryInDateRange(
        userId: String,
        categoryId: String,
        startDate: Long,
        endDate: Long
    ): Result<Double> = try {
        val snapshot = createBaseQuery(userId)
            .whereEqualTo("categoryId", categoryId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .await()

        val total = snapshot.documents.sumOf { 
            it.toObject(getType())?.amount ?: 0.0
        }
        Result.Success(total)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Get transactions by label IDs
    suspend fun getTransactionsByLabels(userId: String, labelIds: List<String>): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQuery(userId)
                .whereArrayContainsAny("labelIds", labelIds)
                .get()
                .await()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions by search term (searches in note field)
    suspend fun searchTransactions(userId: String, searchTerm: String): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQuery(userId)
                .orderBy("note")
                .startAt(searchTerm)
                .endAt(searchTerm + "\uf8ff")
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get recurring transactions
    suspend fun getRecurringTransactions(userId: String): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQuery(userId)
                .whereEqualTo("isRecurring", true)
                .get()
                .await()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions that need to be processed (for recurring transactions)
    suspend fun getTransactionsToProcess(userId: String): Result<List<TransactionDTO>> = try {
        val currentTime = System.currentTimeMillis()
        val snapshot = createBaseQuery(userId)
            .whereEqualTo("isRecurring", true)
            .whereLessThanOrEqualTo("nextOccurrence", currentTime)
            .get()
            .await()

        val transactions = snapshot.documents.mapNotNull { 
            it.toObject(getType())
        }
        Result.Success(transactions)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Real-time listeners
    fun observeTransactionsForUser(userId: String): Flow<List<TransactionDTO>> {
        return observeCollection(userId, createBaseQuery(userId))
    }

    fun observeTransactionsForWallet(userId: String, walletId: String): Flow<List<TransactionDTO>> {
        val query = createBaseQuery(userId)
            .whereEqualTo("walletId", walletId)
        return observeCollection(userId, query)
    }

    fun observeTransactionsForCategory(userId: String, categoryId: String): Flow<List<TransactionDTO>> {
        val query = createBaseQuery(userId)
            .whereEqualTo("categoryId", categoryId)
        return observeCollection(userId, query)
    }

    fun observeTransactionsInDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionDTO>> {
        val query = createBaseQuery(userId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
        return observeCollection(userId, query)
    }

    fun observeTotalAmountInDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<Double> {
        return observeTransactionsInDateRange(userId, startDate, endDate)
            .map { transactions ->
                transactions.sumOf { it.amount }
            }
    }

    fun observeRecurringTransactions(userId: String): Flow<List<TransactionDTO>> {
        val query = createBaseQuery(userId)
            .whereEqualTo("isRecurring", true)
        return observeCollection(userId, query)
    }

    fun observeTransaction(userId: String, transactionId: String): Flow<TransactionDTO?> {
        return observeDocument(userId, transactionId)
    }
} 