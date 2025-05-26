package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.synaptix.budgetbuddy.core.model.Result
import kotlinx.coroutines.tasks.await

abstract class BaseFirestoreRepository<T : Any>(
    protected val firestore: FirebaseFirestore
) {
    protected abstract val collection: CollectionReference

    protected suspend fun create(item: T, id: String? = null): Result<String> {
        return try {
            val docRef = if (id != null) {
                collection.document(id)
            } else {
                collection.document()
            }
            docRef.set(item).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected suspend fun update(id: String, item: T): Result<Unit> {
        return try {
            collection.document(id).set(item).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected suspend fun delete(id: String): Result<Unit> {
        return try {
            collection.document(id).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected suspend fun getById(id: String): Result<T?> {
        return try {
            val snapshot = collection.document(id).get().await()
            Result.Success(snapshot.toObject(getType()))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected suspend fun getAll(query: Query): Result<List<T>> {
        return try {
            val snapshot = query.get().await()
            val items = snapshot.documents.mapNotNull { 
                it.toObject(getType())
            }
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected fun createBaseQuery(): Query = collection

    protected fun createBaseQueryWithUserId(userId: String): Query {
        return collection.whereEqualTo("userId", userId)
    }

    protected suspend fun getItemsByIds(ids: List<String>): Result<List<T>> {
        if (ids.isEmpty()) {
            return Result.Success(emptyList())
        }

        return try {
            val items = mutableListOf<T>()
            ids.chunked(10).forEach { chunk ->
                val snapshots = chunk.map { id ->
                    collection.document(id).get().await()
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
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("name", name)
                .get()
                .await()

            Result.Success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected abstract fun getType(): Class<T>
}