package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.synaptix.budgetbuddy.core.model.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

abstract class BaseFirestoreRepository<T : Any>(
    protected val firestore: FirebaseFirestore
) {
    protected abstract val collection: CollectionReference
    protected abstract val subCollectionName: String?

    protected fun getSubCollection(userId: String): CollectionReference? {
        return subCollectionName?.let {
            firestore.collection("users")
                .document(userId)
                .collection(it)
        }
    }

    protected suspend fun create(item: T, userId: String, id: String? = null): Result<String> {
        return try {
            val targetCollection = getSubCollection(userId) ?: collection
            val docRef = if (id != null) {
                targetCollection.document(id)
            } else {
                targetCollection.document()
            }
            docRef.set(item).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected suspend fun update(userId: String, id: String, item: T): Result<Unit> {
        return try {
            val targetCollection = getSubCollection(userId) ?: collection
            targetCollection.document(id).set(item).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected suspend fun delete(userId: String, id: String): Result<Unit> {
        return try {
            val targetCollection = getSubCollection(userId) ?: collection
            targetCollection.document(id).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected suspend fun getById(userId: String, id: String): Result<T?> {
        return try {
            val targetCollection = getSubCollection(userId) ?: collection
            val snapshot = targetCollection.document(id).get().await()
            Result.Success(snapshot.toObject(getType()))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected suspend fun getAll(userId: String, query: Query): Result<List<T>> {
        return try {
            val targetCollection = getSubCollection(userId) ?: collection
            val snapshot = query.get().await()
            val items = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected fun createBaseQuery(userId: String): Query {
        return getSubCollection(userId) ?: collection
    }

    protected fun createBaseQueryWithUserId(userId: String): Query {
        val targetCollection = getSubCollection(userId) ?: collection
        return targetCollection.whereEqualTo("userId", userId)
    }

    protected suspend fun getItemsByIds(userId: String, ids: List<String>): Result<List<T>> {
        if (ids.isEmpty()) {
            return Result.Success(emptyList())
        }

        return try {
            val targetCollection = getSubCollection(userId) ?: collection
            val items = mutableListOf<T>()
            ids.chunked(10).forEach { chunk ->
                val snapshots = chunk.map { id ->
                    targetCollection.document(id).get().await()
                }
                items.addAll(snapshots.mapNotNull { it.toObject(getType()) })
            }
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected suspend fun checkNameExists(userId: String, name: String): Result<Boolean> {
        return try {
            val targetCollection = getSubCollection(userId) ?: collection
            val snapshot = targetCollection
                .whereEqualTo("name", name)
                .get()
                .await()

            Result.Success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Real-time listener for a single document
    protected fun observeDocument(userId: String, documentId: String): Flow<T?> = callbackFlow {
        val targetCollection = getSubCollection(userId) ?: collection
        val listener = targetCollection.document(documentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(getType()))
            }
        awaitClose { listener.remove() }
    }

    // Real-time listener for a collection
    protected fun observeCollection(userId: String, query: Query? = null): Flow<List<T>> = callbackFlow {
        val targetCollection = getSubCollection(userId) ?: collection
        val finalQuery = query ?: targetCollection
        val listener = finalQuery
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toObject(getType()) } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    protected abstract fun getType(): Class<T>
}