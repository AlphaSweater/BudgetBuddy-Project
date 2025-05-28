package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.WalletDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreWalletRepository @Inject constructor(
    firestore: FirebaseFirestore
) : BaseFirestoreRepository<WalletDTO>(firestore) {
    
    override val collection = firestore.collection("users")
    override val subCollectionName = "wallets"

    override fun getType(): Class<WalletDTO> = WalletDTO::class.java

    // Create a new wallet
    suspend fun createWallet(userId: String, wallet: WalletDTO): Result<String> {
        val newWallet = wallet.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return create(newWallet, userId)
    }

    // Update an existing wallet
    suspend fun updateWallet(userId: String, wallet: WalletDTO): Result<Unit> {
        val updatedWallet = wallet.copy(updatedAt = System.currentTimeMillis())
        return update(userId, wallet.id, updatedWallet)
    }

    // Delete a wallet
    suspend fun deleteWallet(userId: String, walletId: String): Result<Unit> = 
        delete(userId, walletId)

    // Get a single wallet by ID
    suspend fun getWalletById(userId: String, walletId: String): Result<WalletDTO?> =
        getById(userId, walletId)

    // Get all wallets for a user
    suspend fun getWalletsForUser(userId: String): Result<List<WalletDTO>> {
        return try {
            val snapshot = createBaseQuery(userId).get().await()
            val wallets = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(wallets)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Update wallet balance
    suspend fun updateWalletBalance(userId: String, walletId: String, amount: Double): Result<Unit> {
        return try {
            val wallet = when (val result = getWalletById(userId, walletId)) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error(result.exception)
            }

            val updatedWallet = wallet!!.updateBalance(amount)
            update(userId, walletId, updatedWallet)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get total balance for a user (excluding wallets marked as excludeFromTotal)
    suspend fun getTotalBalanceForUser(userId: String): Result<Double> {
        return try {
            val snapshot = createBaseQuery(userId)
                .get()
                .await()

            val total = snapshot.documents
                .mapNotNull { it.toObject(getType()) }
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
                id = "default",
                userId = userId,
                name = "Main Wallet",
                currency = "USD",
                balance = 0.0,
                excludeFromTotal = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            createWallet(userId, defaultWallet)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Real-time listeners
    fun observeWalletsForUser(userId: String): Flow<List<WalletDTO>> {
        return observeCollection(userId, createBaseQuery(userId))
    }

    fun observeWallet(userId: String, walletId: String): Flow<WalletDTO?> {
        return observeDocument(userId, walletId)
    }

    fun observeTotalBalance(userId: String): Flow<Double> {
        return observeCollection(userId, createBaseQuery(userId))
            .map { wallets ->
                wallets.filter { !it.excludeFromTotal }
                    .sumOf { it.balance }
            }
    }
}
