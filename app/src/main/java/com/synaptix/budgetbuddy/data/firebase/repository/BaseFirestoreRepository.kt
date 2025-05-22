package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.synaptix.budgetbuddy.core.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

abstract class BaseFirestoreRepository<T>(
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
            if (item != null) {
                docRef.set(item).await()
            }
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected suspend fun update(id: String, item: T): Result<Unit> {
        return try {
            if (item != null) {
                collection.document(id).set(item).await()
            }
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

    protected fun getById(id: String, mapper: (DocumentReference) -> Flow<Result<T?>>): Flow<Result<T?>> {
        return mapper(collection.document(id))
    }

    protected fun getAll(query: Query, mapper: (Query) -> Flow<Result<List<T>>>): Flow<Result<List<T>>> {
        return mapper(query)
    }

    protected fun createBaseQuery(): Query = collection

    protected fun createBaseQueryWithUserId(userId: String): Query {
        return collection.whereEqualTo("userId", userId)
    }

    protected fun getItemsByIds(ids: List<String>, mapper: (DocumentSnapshot) -> T?): Flow<Result<List<T>>> = flow {
        if (ids.isEmpty()) {
            emit(Result.Success(emptyList()))
            return@flow
        }

        try {
            val items = mutableListOf<T>()
            ids.chunked(10).forEach { chunk ->
                val snapshots = chunk.map { id ->
                    collection.document(id).get().await()
                }
                items.addAll(snapshots.mapNotNull { mapper(it) })
            }
            emit(Result.Success(items))
        } catch (e: Exception) {
            emit(Result.Error(e))
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
}