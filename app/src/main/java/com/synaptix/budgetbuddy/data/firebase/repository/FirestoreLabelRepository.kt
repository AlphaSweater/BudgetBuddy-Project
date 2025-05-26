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
    firestore: FirebaseFirestore
) : BaseFirestoreRepository<LabelDTO>(firestore) {
    
    override val collection = firestore.collection("labels")

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
    fun getLabelsForUser(userId: String): Flow<Result<List<LabelDTO>>> = getAll(createBaseQueryWithUserId(userId)) { query ->
        callbackFlow {
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
} 