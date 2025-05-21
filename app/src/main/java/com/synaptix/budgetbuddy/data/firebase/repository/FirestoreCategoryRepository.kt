package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.CategoryDTO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCategoryRepository @Inject constructor(
    private val firestoreInstance: FirebaseFirestore
) : BaseFirestoreRepository<CategoryDTO>(firestoreInstance) {

    override val collection = firestoreInstance.collection("categories")

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

    fun getCategoryById(categoryId: String): Flow<Result<CategoryDTO?>> = getById(categoryId) { docRef ->
        callbackFlow {
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                    return@addSnapshotListener
                }
                val category = snapshot?.toObject(CategoryDTO::class.java)
                trySend(Result.Success(category))
            }
            awaitClose { listener.remove() }
        }
    }

    fun getCategoriesByIds(categoryIds: List<String>): Flow<Result<List<CategoryDTO>>> {
        return getItemsByIds(categoryIds) { snapshot ->
            snapshot.toObject(CategoryDTO::class.java)
        }
    }

    fun getCategoriesForUser(userId: String): Flow<Result<List<CategoryDTO>>> = callbackFlow {
        val userQuery = collection.whereEqualTo("userId", userId)
        val defaultQuery = collection.whereEqualTo("isDefault", true)

        val userListener = userQuery.addSnapshotListener { snapshot1, error1 ->
            if (error1 != null) {
                trySend(Result.Error(error1))
                return@addSnapshotListener
            }

            defaultQuery.get().addOnSuccessListener { snapshot2 ->
                val userCategories = snapshot1?.documents?.mapNotNull { it.toObject(CategoryDTO::class.java) } ?: emptyList()
                val defaultCategories = snapshot2?.documents?.mapNotNull { it.toObject(CategoryDTO::class.java) } ?: emptyList()
                trySend(Result.Success(userCategories + defaultCategories))
            }.addOnFailureListener { trySend(Result.Error(it)) }
        }

        awaitClose { userListener.remove() }
    }

    fun getCategoriesByType(userId: String, type: String): Flow<Result<List<CategoryDTO>>> = callbackFlow {
        val userQuery = collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", type)

        val defaultQuery = collection
            .whereEqualTo("isDefault", true)
            .whereEqualTo("type", type)

        val userListener = userQuery.addSnapshotListener { snapshot1, error1 ->
            if (error1 != null) {
                trySend(Result.Error(error1))
                return@addSnapshotListener
            }

            defaultQuery.get().addOnSuccessListener { snapshot2 ->
                val userCategories = snapshot1?.documents?.mapNotNull { it.toObject(CategoryDTO::class.java) } ?: emptyList()
                val defaultCategories = snapshot2?.documents?.mapNotNull { it.toObject(CategoryDTO::class.java) } ?: emptyList()
                trySend(Result.Success(userCategories + defaultCategories))
            }.addOnFailureListener { trySend(Result.Error(it)) }
        }

        awaitClose { userListener.remove() }
    }

    suspend fun createDefaultCategories(): Result<Unit> {
        return try {
            val defaultCategories = listOf(
                CategoryDTO(
                    userId = null,
                    name = "Food",
                    type = "expense",
                    color = android.graphics.Color.parseColor("#FF1493"), // cat_dark_pink
                    icon = android.R.drawable.ic_menu_compass,
                    isDefault = true
                ),
                CategoryDTO(
                    userId = null,
                    name = "Transport",
                    type = "expense",
                    color = android.graphics.Color.parseColor("#FFD700"), // cat_yellow
                    icon = android.R.drawable.ic_menu_directions,
                    isDefault = true
                ),
                CategoryDTO(
                    userId = null,
                    name = "HealthCare",
                    type = "expense",
                    color = android.graphics.Color.parseColor("#FFA500"), // cat_gold
                    icon = android.R.drawable.ic_menu_help,
                    isDefault = true
                ),
                CategoryDTO(
                    userId = null,
                    name = "Beauty",
                    type = "expense",
                    color = android.graphics.Color.parseColor("#800080"), // cat_dark_purple
                    icon = android.R.drawable.ic_menu_gallery,
                    isDefault = true
                ),
                CategoryDTO(
                    userId = null,
                    name = "Bills & Fees",
                    type = "expense",
                    color = android.graphics.Color.parseColor("#90EE90"), // cat_light_green
                    icon = android.R.drawable.ic_menu_save,
                    isDefault = true
                ),
                CategoryDTO(
                    userId = null,
                    name = "Education",
                    type = "expense",
                    color = android.graphics.Color.parseColor("#FFD700"), // cat_yellow
                    icon = android.R.drawable.ic_menu_edit,
                    isDefault = true
                ),
                CategoryDTO(
                    userId = null,
                    name = "Entertainment",
                    type = "expense",
                    color = android.graphics.Color.parseColor("#E6E6FA"), // cat_light_purple
                    icon = android.R.drawable.ic_menu_view,
                    isDefault = true
                ),
                CategoryDTO(
                    userId = null,
                    name = "Family & Friends",
                    type = "expense",
                    color = android.graphics.Color.parseColor("#00008B"), // cat_dark_blue
                    icon = android.R.drawable.ic_menu_myplaces,
                    isDefault = true
                ),
                CategoryDTO(
                    userId = null,
                    name = "Groceries",
                    type = "expense",
                    color = android.graphics.Color.parseColor("#ADD8E6"), // cat_light_blue
                    icon = android.R.drawable.ic_menu_compass,
                    isDefault = true
                ),
                CategoryDTO(
                    userId = null,
                    name = "Salary",
                    type = "Income",
                    color = android.graphics.Color.parseColor("#ADD8E6"), // cat_light_blue
                    icon = android.R.drawable.ic_menu_save,
                    isDefault = true
                )
            )

            val batch = firestoreInstance.batch()
            defaultCategories.forEach { category ->
                val docRef = collection.document()
                batch.set(docRef, category.copy(
                    id = docRef.id,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ))
            }
            batch.commit().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun categoryNameExists(userId: String, name: String): Result<Boolean> =
        checkNameExists(userId, name)
}