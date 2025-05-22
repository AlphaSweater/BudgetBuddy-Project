package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.WalletDTO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreWalletRepository @Inject constructor(
    firestore: FirebaseFirestore
) : BaseFirestoreRepository<WalletDTO>(firestore) {
    
    override val collection = firestore.collection("wallets")

    // Create a new wallet
    suspend fun createWallet(wallet: WalletDTO): Result<String> {
        val newWallet = wallet.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return create(newWallet)
    }

    // Update an existing wallet
    suspend fun updateWallet(wallet: WalletDTO): Result<Unit> {
        val updatedWallet = wallet.copy(updatedAt = System.currentTimeMillis())
        return update(wallet.id, updatedWallet)
    }

    // Delete a wallet
    suspend fun deleteWallet(walletId: String): Result<Unit> = delete(walletId)

    // Get a single wallet by ID
    fun getWalletById(walletId: String): Flow<Result<WalletDTO?>> = getById(walletId) { docRef ->
        callbackFlow {
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                    return@addSnapshotListener
                }
                val wallet = snapshot?.toObject(WalletDTO::class.java)
                trySend(Result.Success(wallet))
            }
            awaitClose { listener.remove() }
        }
    }

    // Get all wallets for a user
    fun getWalletsForUser(userId: String): Flow<Result<List<WalletDTO>>> = getAll(createBaseQueryWithUserId(userId)) { query ->
        callbackFlow {
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                    return@addSnapshotListener
                }
                val wallets = snapshot?.documents?.mapNotNull { 
                    it.toObject(WalletDTO::class.java)
                } ?: emptyList()
                trySend(Result.Success(wallets))
            }
            awaitClose { listener.remove() }
        }
    }

    // Update wallet balance
    suspend fun updateWalletBalance(walletId: String, amount: Double): Result<Unit> {
        return try {
            val wallet = collection.document(walletId).get().await()
                .toObject(WalletDTO::class.java)
                ?: return Result.Error(Exception("Wallet not found"))

            val updatedWallet = wallet.updateBalance(amount)
            update(walletId, updatedWallet)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get total balance for a user (excluding wallets marked as excludeFromTotal)
    suspend fun getTotalBalanceForUser(userId: String): Result<Double> {
        return try {
            val snapshot = createBaseQueryWithUserId(userId)
                .get()
                .await()

            val total = snapshot.documents
                .mapNotNull { it.toObject(WalletDTO::class.java) }
                .filter { !it.excludeFromTotal }
                .sumOf { it.balance }

            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Check if a wallet name already exists for a user
    suspend fun walletNameExists(userId: String, name: String): Result<Boolean> = 
        checkNameExists(userId, name)

    // Create default wallet for a new user
    suspend fun createDefaultWallet(userId: String): Result<String> {
        return try {
            val defaultWallet = WalletDTO(
                userId = userId,
                name = "Main Wallet",
                currency = "USD",
                balance = 0.0,
                excludeFromTotal = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            createWallet(defaultWallet)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
