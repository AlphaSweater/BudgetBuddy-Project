package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.LabelDTO
import com.synaptix.budgetbuddy.data.firebase.model.TransactionDTO
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreTransactionRepository @Inject constructor(
    private val firestoreInstance: FirebaseFirestore
) : BaseFirestoreRepository<TransactionDTO>(firestoreInstance) {
    
    override val collection = firestoreInstance.collection("transactions")

    override fun getType(): Class<TransactionDTO> = TransactionDTO::class.java

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
    suspend fun getTransactionById(transactionId: String): Result<TransactionDTO?> {
        return try {
            val snapshot = collection.document(transactionId).get().await()
            Result.Success(snapshot.toObject(TransactionDTO::class.java))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get all transactions for a user based on userId
    suspend fun getTransactionsForUser(userId: String): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQueryWithUserId(userId).get().await()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions for a specific wallet
    suspend fun getTransactionsForWallet(walletId: String): Result<List<TransactionDTO>> {
        return try {
            val snapshot = collection.whereEqualTo("walletId", walletId).get().await()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions for a specific category
    suspend fun getTransactionsForCategory(categoryId: String): Result<List<TransactionDTO>> {
        return try {
            val snapshot = collection.whereEqualTo("categoryId", categoryId).get().await()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
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
    ): Result<Double> = try {
        val snapshot = createBaseQueryWithUserId(userId)
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

    // Get transactions for a specific user within a date range
    suspend fun getTransactionsForUserInDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<TransactionDTO>> {
        return try {
            val snapshot = createBaseQueryWithUserId(userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
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

    // Get transactions for a specific wallet within a date range
    suspend fun getTransactionsForWalletInDateRange(
        walletId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<TransactionDTO>> {
        return try {
            val snapshot = collection
                .whereEqualTo("walletId", walletId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
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

    // Get transactions for a specific category within a date range
    suspend fun getTransactionsForCategoryInDateRange(
        categoryId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<TransactionDTO>> {
        return try {
            val snapshot = collection
                .whereEqualTo("categoryId", categoryId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
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

    // Get total amount for a wallet in a date range
    suspend fun getTotalAmountForWalletInDateRange(
        walletId: String,
        startDate: Long,
        endDate: Long
    ): Result<Double> = try {
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

    // Get total amount for a category in a date range
    suspend fun getTotalAmountForCategoryInDateRange(
        categoryId: String,
        startDate: Long,
        endDate: Long
    ): Result<Double> = try {
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

    // Get transactions by label IDs
    suspend fun getTransactionsByLabels(labelIds: List<String>): Result<List<TransactionDTO>> {
        return try {
            val snapshot = collection.whereArrayContainsAny("labelIds", labelIds).get().await()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions by search term (searches in note field)
    suspend fun searchTransactions(searchTerm: String): Result<List<TransactionDTO>> {
        return try {
            val snapshot = collection
                .orderBy("note")
                .startAt(searchTerm)
                .endAt(searchTerm + "\uf8ff")
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

    // Get recurring transactions
    suspend fun getRecurringTransactions(): Result<List<TransactionDTO>> {
        return try {
            val snapshot = collection.whereEqualTo("isRecurring", true).get().await()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(TransactionDTO::class.java)
            }
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get transactions that need to be processed (for recurring transactions)
    suspend fun getTransactionsToProcess(): Result<List<TransactionDTO>> = try {
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