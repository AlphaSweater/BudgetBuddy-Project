package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.LabelDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreLabelRepository @Inject constructor(
    firestore: FirebaseFirestore
) : BaseFirestoreRepository<LabelDTO>(firestore) {
    
    override val collection = firestore.collection("users")
    override val subCollectionName = "labels"

    override fun getType(): Class<LabelDTO> = LabelDTO::class.java

    // Create a new label
    suspend fun createLabel(userId: String, label: LabelDTO): Result<String> {
        val newLabel = label.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return create(newLabel, userId)
    }

    // Update an existing label
    suspend fun updateLabel(userId: String, label: LabelDTO): Result<Unit> {
        val updatedLabel = label.copy(updatedAt = System.currentTimeMillis())
        return update(userId, label.id, updatedLabel)
    }

    // Delete a label
    suspend fun deleteLabel(userId: String, labelId: String): Result<Unit> = 
        delete(userId, labelId)

    // Get a single label by ID
    suspend fun getLabelById(userId: String, labelId: String): Result<LabelDTO?> =
        getById(userId, labelId)

    // Get multiple labels by their IDs in a single batch
    suspend fun getLabelsByIds(userId: String, labelIds: List<String>): Result<List<LabelDTO>> =
        getItemsByIds(userId, labelIds)

    // Get all labels for a user
    suspend fun getLabelsForUser(userId: String): Result<List<LabelDTO>> {
        return try {
            val userSnapshot = createBaseQuery(userId)
                .whereEqualTo("isDefault", false)
                .get()
                .await()

            val defaultSnapshot = collection
                .document("default")
                .collection(subCollectionName!!)
                .whereEqualTo("isDefault", true)
                .get()
                .await()

            val userLabels = userSnapshot.documents.mapNotNull { it.toObject(getType()) }
            val defaultLabels = defaultSnapshot.documents.mapNotNull { it.toObject(getType()) }
            
            Result.Success(userLabels + defaultLabels)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get default labels
    suspend fun getDefaultLabels(): Result<List<LabelDTO>> {
        return try {
            val snapshot = collection
                .document("default")
                .collection(subCollectionName!!)
                .whereEqualTo("isDefault", true)
                .get()
                .await()
            
            val labels = snapshot.documents.mapNotNull { 
                it.toObject(getType())
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
        val defaultCollection = collection
            .document("default")
            .collection(subCollectionName!!)

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
            val label = LabelDTO(
                id = labelId,
                userId = null,
                name = labelName,
                isDefault = true,
                createdAt = now,
                updatedAt = now
            )

            defaultCollection.document(labelId)
                .set(label)
                .await()
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Real-time listeners
    fun observeLabelsForUser(userId: String): Flow<List<LabelDTO>> {
        return observeCollection(userId, createBaseQuery(userId))
    }

    fun observeLabels(userId: String, labelIds: List<String>): Flow<List<LabelDTO>> {
        val labelCollection = collection
            .document(userId)
            .collection(subCollectionName)
        return observeCollection(userId, labelCollection.whereIn("id", labelIds))

    }

    fun observeDefaultLabels(): Flow<List<LabelDTO>> {
        val defaultCollection = collection
            .document("default")
            .collection(subCollectionName!!)
        return observeCollection("default", defaultCollection)
    }

    fun observeLabel(userId: String, labelId: String): Flow<LabelDTO?> {
        return observeDocument(userId, labelId)
    }
} 