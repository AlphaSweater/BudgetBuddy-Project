//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.synaptix.budgetbuddy.data.entity.UserEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// ===================================
// DataStore Setup
// ===================================
private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // ===================================
    // Preference Keys (Private)
    // ===================================
    private object PreferencesKeys {
        val USER_ID = intPreferencesKey("user_id")
        val USER_FIRSTNAME = stringPreferencesKey("user_firstname")
        val USER_LASTNAME = stringPreferencesKey("user_lastname")
        val USER_EMAIL = stringPreferencesKey("user_email")

        val SELECTED_WALLET_ID = intPreferencesKey("selected_wallet_id")
        val SELECTED_BUDGET_ID = intPreferencesKey("selected_budget_id")
    }

    // ===================================
    // Save User to DataStore
    // ===================================
    suspend fun saveUser(user: UserEntity) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_ID] = user.user_id
            prefs[PreferencesKeys.USER_FIRSTNAME] = user.firstName.toString()
            prefs[PreferencesKeys.USER_LASTNAME] = user.lastName.toString()
            prefs[PreferencesKeys.USER_EMAIL] = user.email
        }
    }

    // ===================================
    // Save Selected Wallet ID
    // ===================================
    suspend fun saveSelectedWalletId(walletId: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_WALLET_ID] = walletId
        }
    }

    // ===================================
    // Save Selected Budget ID
    // ===================================
    suspend fun saveSelectedBudgetId(budgetId: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_BUDGET_ID] = budgetId
        }
    }

    // ===================================
    // Get Selected Wallet ID
    // ===================================
    suspend fun getSelectedWalletId(): Int? {
        val prefs = dataStore.data.first()
        return prefs[PreferencesKeys.SELECTED_WALLET_ID]
    }

    // ===================================
    // Get Selected Budget ID
    // ===================================
    suspend fun getSelectedBudgetId(): Int? {
        val prefs = dataStore.data.first()
        return prefs[PreferencesKeys.SELECTED_BUDGET_ID]
    }

    // ===================================
    // Get User ID
    // ===================================
    suspend fun getUserId(): Int {
        val preferences = dataStore.data.first()
        return preferences[PreferencesKeys.USER_ID] ?: 0 // Default to 0 if null
    }

    // ===================================
    // Clear User Data
    // ===================================
    suspend fun clearUser() {
        dataStore.edit { it.clear() }
    }

    // ===================================
    // User Flow (Reactive Stream)
    // ===================================
    val userFlow: Flow<UserEntity?> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            val id = prefs[PreferencesKeys.USER_ID]
            val firstName = prefs[PreferencesKeys.USER_FIRSTNAME]
            val lastName = prefs[PreferencesKeys.USER_LASTNAME]
            val email = prefs[PreferencesKeys.USER_EMAIL]

            if (id != null && firstName != null && lastName != null && email != null) {
                UserEntity(id, firstName, lastName, email, password = "")
            } else {
                null
            }
        }
}
