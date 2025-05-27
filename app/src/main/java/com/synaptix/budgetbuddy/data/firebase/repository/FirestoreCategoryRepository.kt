package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.CategoryDTO
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCategoryRepository @Inject constructor(
    private val firestoreInstance: FirebaseFirestore
) : BaseFirestoreRepository<CategoryDTO>(firestoreInstance) {

    override val collection = firestoreInstance.collection("categories")

    override fun getType(): Class<CategoryDTO> = CategoryDTO::class.java

    suspend fun createCategory(category: CategoryDTO): Result<String> {
        val newCategory = category.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return create(newCategory)
    }

    suspend fun updateCategory(category: CategoryDTO): Result<Unit> {
        val updatedCategory = category.copy(updatedAt = System.currentTimeMillis())
        return update(category.id, updatedCategory)
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = delete(categoryId)

    suspend fun getCategoryById(categoryId: String): Result<CategoryDTO?> {
        return try {
            val snapshot = collection.document(categoryId).get().await()
            Result.Success(snapshot.toObject(CategoryDTO::class.java))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getCategoriesByIds(categoryIds: List<String>): Result<List<CategoryDTO>> {
        return try {
            val items = mutableListOf<CategoryDTO>()
            categoryIds.chunked(10).forEach { chunk ->
                val snapshots = chunk.map { id ->
                    collection.document(id).get().await()
                }
                items.addAll(snapshots.mapNotNull { it.toObject(CategoryDTO::class.java) })
            }
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getCategoriesForUser(userId: String): Result<List<CategoryDTO>> {
        return try {
            val userSnapshot = collection.whereEqualTo("userId", userId).get().await()
            val defaultSnapshot = collection.whereEqualTo("isDefault", true).get().await()

            val userCategories = userSnapshot.documents.mapNotNull { it.toObject(CategoryDTO::class.java) }
            val defaultCategories = defaultSnapshot.documents.mapNotNull { it.toObject(CategoryDTO::class.java) }
            
            Result.Success(userCategories + defaultCategories)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getCategoriesByType(userId: String, type: String): Result<List<CategoryDTO>> {
        return try {
            val userSnapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .get()
                .await()

            val defaultSnapshot = collection
                .whereEqualTo("isDefault", true)
                .whereEqualTo("type", type)
                .get()
                .await()

            val userCategories = userSnapshot.documents.mapNotNull { it.toObject(CategoryDTO::class.java) }
            val defaultCategories = defaultSnapshot.documents.mapNotNull { it.toObject(CategoryDTO::class.java) }
            
            Result.Success(userCategories + defaultCategories)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun categoryNameExists(userId: String, name: String): Result<Boolean> =
        checkNameExists(userId, name)
}