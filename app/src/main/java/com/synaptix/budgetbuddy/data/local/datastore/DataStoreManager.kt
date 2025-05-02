package com.synaptix.budgetbuddy.data.local.datastore

import android.content.Context

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.synaptix.budgetbuddy.data.entity.UserEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // 🔐 Keep keys private to avoid accidental misuse
    private object PreferencesKeys {
        val USER_ID = intPreferencesKey("user_id")
        val USER_FIRSTNAME = stringPreferencesKey("user_firstname")
        val USER_LASTNAME = stringPreferencesKey("user_lastname")
        val USER_EMAIL = stringPreferencesKey("user_email")

        val SELECTED_WALLET_ID = intPreferencesKey("selected_wallet_id")
        val SELECTED_BUDGET_ID = intPreferencesKey("selected_budget_id")
    }

    // ✅ Save only non-sensitive info
    suspend fun saveUser(user: UserEntity) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_ID] = user.user_id
            prefs[PreferencesKeys.USER_FIRSTNAME] = user.firstName.toString()
            prefs[PreferencesKeys.USER_LASTNAME] = user.lastName.toString()
            prefs[PreferencesKeys.USER_EMAIL] = user.email
        }
    }

    suspend fun saveSelectedWalletId(walletId: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_WALLET_ID] = walletId
        }
    }

    suspend fun saveSelectedBudgetId(budgetId: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_BUDGET_ID] = budgetId
        }
    }

    suspend fun getSelectedWalletId(): Int? {
        val prefs = dataStore.data.first()
        return prefs[PreferencesKeys.SELECTED_WALLET_ID]
    }

    suspend fun getSelectedBudgetId(): Int? {
        val prefs = dataStore.data.first()
        return prefs[PreferencesKeys.SELECTED_BUDGET_ID]
    }



    // Fetch user ID
    suspend fun getUserId(): Int {
        val preferences = dataStore.data.first()
        return preferences[PreferencesKeys.USER_ID] ?: 0 // Default to 0 if null
    }

    suspend fun clearUser() {
        dataStore.edit { it.clear() }
    }

    // 🔄 Safe Flow with basic error handling
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