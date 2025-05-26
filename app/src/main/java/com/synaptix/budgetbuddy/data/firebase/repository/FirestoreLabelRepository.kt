package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.LabelDTO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreLabelRepository @Inject constructor(
    private val firestoreInstance: FirebaseFirestore
) : BaseFirestoreRepository<LabelDTO>(firestoreInstance) {
    
    override val collection = firestoreInstance.collection("labels")

    // Create a new label
    suspend fun createLabel(label: LabelDTO): Result<String> {
        val newLabel = label.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return create(newLabel)
    }

    // Update an existing label
    suspend fun updateLabel(label: LabelDTO): Result<Unit> {
        val updatedLabel = label.copy(updatedAt = System.currentTimeMillis())
        return update(label.id, updatedLabel)
    }

    // Delete a label
    suspend fun deleteLabel(labelId: String): Result<Unit> = delete(labelId)

    // Get a single label by ID
    fun getLabelById(labelId: String): Flow<Result<LabelDTO?>> = getById(labelId) { docRef ->
        callbackFlow {
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                    return@addSnapshotListener
                }
                val label = snapshot?.toObject(LabelDTO::class.java)
                trySend(Result.Success(label))
            }
            awaitClose { listener.remove() }
        }
    }

    // Get multiple labels by their IDs in a single batch
    fun getLabelsByIds(labelIds: List<String>): Flow<Result<List<LabelDTO>>> {
        return getItemsByIds(labelIds) { snapshot ->
            snapshot.toObject(LabelDTO::class.java)
        }
    }

    // Get all labels for a user
    fun getLabelsForUser(userId: String): Flow<Result<List<LabelDTO>>> = callbackFlow {
        val userQuery = collection.whereEqualTo("userId", userId)
        val defaultQuery = collection.whereEqualTo("isDefault", true)

        val userListener = userQuery.addSnapshotListener { snapshot1, error1 ->
            if (error1 != null) {
                trySend(Result.Error(error1))
                return@addSnapshotListener
            }

            defaultQuery.get().addOnSuccessListener { snapshot2 ->
                val userLabels = snapshot1?.documents?.mapNotNull { it.toObject(LabelDTO::class.java) } ?: emptyList()
                val defaultLabels = snapshot2?.documents?.mapNotNull { it.toObject(LabelDTO::class.java) } ?: emptyList()
                trySend(Result.Success(userLabels + defaultLabels))
            }.addOnFailureListener { trySend(Result.Error(it)) }
        }

        awaitClose { userListener.remove() }
    }

    // Get default labels (where userId is null)
    fun getDefaultLabels(): Flow<Result<List<LabelDTO>>> = callbackFlow {
        val query = collection.whereEqualTo("userId", null)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            val labels = snapshot?.documents?.mapNotNull { 
                it.toObject(LabelDTO::class.java)
            } ?: emptyList()
            trySend(Result.Success(labels))
        }

        awaitClose { listener.remove() }
    }

    suspend fun labelNameExists(userId: String, name: String): Result<Boolean> =
        checkNameExists(userId, name)

    suspend fun createDefaultLabels(): Result<Unit> = try {
        val now = System.currentTimeMillis()

        val defaultLabels = listOf(
            "Needs",
            "Wants",
            "Recurring",
            "Essential",
            "Impulse",
            "Investment"
        )

        defaultLabels.forEachIndexed { index, labelName ->
            val labelId = "globalLabel${index + 1}"
            val labelData = mapOf(
                "id" to labelId,
                "userId" to null,
                "name" to labelName,
                "isDefault" to true,
                "createdAt" to now,
                "updatedAt" to now
            )

            collection.document(labelId)
                .set(labelData)
                .await()
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
} 