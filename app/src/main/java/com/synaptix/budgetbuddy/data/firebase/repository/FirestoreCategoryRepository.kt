package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.CategoryDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCategoryRepository @Inject constructor(
    firestore: FirebaseFirestore
) : BaseFirestoreRepository<CategoryDTO>(firestore) {

    override val collection = firestore.collection("users")
    override val subCollectionName = "categories"

    override fun getType(): Class<CategoryDTO> = CategoryDTO::class.java

    suspend fun createCategory(userId: String, category: CategoryDTO): Result<String> {
        val newCategory = category.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return create(newCategory, userId)
    }

    suspend fun updateCategory(userId: String, category: CategoryDTO): Result<Unit> {
        val updatedCategory = category.copy(updatedAt = System.currentTimeMillis())
        return update(userId, category.id, updatedCategory)
    }

    suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit> = 
        delete(userId, categoryId)

    suspend fun getCategoryById(userId: String, categoryId: String): Result<CategoryDTO?> =
        getById(userId, categoryId)

    suspend fun getCategoriesByIds(userId: String, categoryIds: List<String>): Result<List<CategoryDTO>> =
        getItemsByIds(userId, categoryIds)

    suspend fun getCategoriesForUser(userId: String): Result<List<CategoryDTO>> {
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

            val userCategories = userSnapshot.documents.mapNotNull { it.toObject(getType()) }
            val defaultCategories = defaultSnapshot.documents.mapNotNull { it.toObject(getType()) }
            
            Result.Success(userCategories + defaultCategories)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getCategoriesByType(userId: String, type: String): Result<List<CategoryDTO>> {
        return try {
            val userSnapshot = createBaseQuery(userId)
                .whereEqualTo("type", type)
                .whereEqualTo("isDefault", false)
                .get()
                .await()

            val defaultSnapshot = collection
                .document("default")
                .collection(subCollectionName)
                .whereEqualTo("type", type)
                .whereEqualTo("isDefault", true)
                .get()
                .await()

            val userCategories = userSnapshot.documents.mapNotNull { it.toObject(getType()) }
            val defaultCategories = defaultSnapshot.documents.mapNotNull { it.toObject(getType()) }
            
            Result.Success(userCategories + defaultCategories)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun categoryNameExists(userId: String, name: String): Result<Boolean> =
        checkNameExists(userId, name)

    // Real-time listeners
    fun observeCategoriesForUser(userId: String): Flow<List<CategoryDTO>> {
        return observeCollection(userId, createBaseQuery(userId))
    }

    fun observeCategoriesByType(userId: String, type: String): Flow<List<CategoryDTO>> {
        val query = createBaseQuery(userId)
            .whereEqualTo("type", type)
        return observeCollection(userId, query)
    }

    fun observeCategory(userId: String, categoryId: String): Flow<CategoryDTO?> {
        return observeDocument(userId, categoryId)
    }

    /**
     * Gets only the type field for a specific category.
     * This is a lightweight operation that only fetches the type field.
     * 
     * @param userId The ID of the user who owns the category
     * @param categoryId The ID of the category to fetch
     * @return Result containing the type of the category ("income" or "expense")
     */
    suspend fun getCategoryType(userId: String, categoryId: String): Result<String> {
        return try {
            val targetCollection = getSubCollection(userId) ?: collection
            val doc = targetCollection.document(categoryId).get().await()
            Result.Success(doc.getString("type") ?: "expense")
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}