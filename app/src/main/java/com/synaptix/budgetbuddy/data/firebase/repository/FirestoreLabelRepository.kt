package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.LabelDTO
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreLabelRepository @Inject constructor(
    private val firestoreInstance: FirebaseFirestore
) : BaseFirestoreRepository<LabelDTO>(firestoreInstance) {
    
    override val collection = firestoreInstance.collection("labels")

    override fun getType(): Class<LabelDTO> = LabelDTO::class.java

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
    suspend fun getLabelById(labelId: String): Result<LabelDTO?> {
        return try {
            val snapshot = collection.document(labelId).get().await()
            Result.Success(snapshot.toObject(LabelDTO::class.java))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get multiple labels by their IDs in a single batch
    suspend fun getLabelsByIds(labelIds: List<String>): Result<List<LabelDTO>> {
        return try {
            val items = mutableListOf<LabelDTO>()
            labelIds.chunked(10).forEach { chunk ->
                val snapshots = chunk.map { id ->
                    collection.document(id).get().await()
                }
                items.addAll(snapshots.mapNotNull { it.toObject(LabelDTO::class.java) })
            }
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get all labels for a user
    suspend fun getLabelsForUser(userId: String): Result<List<LabelDTO>> {
        return try {
            val userSnapshot = collection.whereEqualTo("userId", userId).get().await()
            val defaultSnapshot = collection.whereEqualTo("isDefault", true).get().await()

            val userLabels = userSnapshot.documents.mapNotNull { it.toObject(LabelDTO::class.java) }
            val defaultLabels = defaultSnapshot.documents.mapNotNull { it.toObject(LabelDTO::class.java) }
            
            Result.Success(userLabels + defaultLabels)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get default labels (where userId is null)
    suspend fun getDefaultLabels(): Result<List<LabelDTO>> {
        return try {
            val snapshot = collection.whereEqualTo("userId", null).get().await()
            val labels = snapshot.documents.mapNotNull { 
                it.toObject(LabelDTO::class.java)
            }
            Result.Success(labels)
        } catch (e: Exception) {
            Result.Error(e)
        }
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