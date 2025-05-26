package com.synaptix.budgetbuddy.data.firebase.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.synaptix.budgetbuddy.core.model.Result
import com.synaptix.budgetbuddy.data.firebase.model.UserDTO
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val usersCollection = firestore.collection("users")

    // Get current Firebase authenticated user
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Get current user ID
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Register a new user
    suspend fun registerUser(email: String, password: String, userData: UserDTO): Result<UserDTO> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Failed to create user")

            val newUser = userData.copy(
                id = firebaseUser.uid,
                email = email,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            usersCollection.document(firebaseUser.uid).set(newUser).await()
            Result.Success(newUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Login existing user
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Failed to login user")

            // Update login time
            usersCollection.document(firebaseUser.uid)
                .update("lastLoginAt", System.currentTimeMillis())
                .await()

            Result.Success(firebaseUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Logout current user
    fun logoutUser(): Result<Unit> {
        return try {
            auth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get user profile
    suspend fun getUserProfile(userId: String): Result<UserDTO?> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            Result.Success(snapshot.toObject(UserDTO::class.java))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Get current user profile
    suspend fun getCurrentUserProfile(): Result<UserDTO?> {
        val userId = getCurrentUserId() ?: return Result.Error(Exception("No user logged in"))
        return getUserProfile(userId)
    }

    // Update user profile with provided fields
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val dataWithTimestamp = updates + mapOf("updatedAt" to System.currentTimeMillis())
            usersCollection.document(userId).update(dataWithTimestamp).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Delete current user's account
    suspend fun deleteUserAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")

            // Delete Firestore document
            usersCollection.document(user.uid).delete().await()

            // Delete from Firebase Auth
            user.delete().await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Check if email is already registered
    suspend fun emailExists(email: String): Result<Boolean> {
        return try {
            val methods = auth.fetchSignInMethodsForEmail(email).await()
            Result.Success(methods.signInMethods?.isNotEmpty() == true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Send password reset email
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}