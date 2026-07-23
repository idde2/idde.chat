package de.eddi.chat.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Singleton DataStore
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenStorage(private val context: Context) {

    companion object {
        private val KEY_TOKEN    = stringPreferencesKey("jwt_token")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_USER_ID  = intPreferencesKey("user_id")
        private val KEY_DARK_MODE = androidx.datastore.preferences.core.booleanPreferencesKey("dark_mode")
        private val KEY_NOTIFS    = androidx.datastore.preferences.core.booleanPreferencesKey("notifications_enabled")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }
    val usernameFlow: Flow<String?> = context.dataStore.data.map { it[KEY_USERNAME] }
    val userIdFlow: Flow<Int?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val darkModeFlow: Flow<Boolean?> = context.dataStore.data.map { it[KEY_DARK_MODE] }
    val notificationsFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFS] ?: true }

    suspend fun saveAuth(token: String, username: String, userId: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN]    = token
            prefs[KEY_USERNAME] = username
            prefs[KEY_USER_ID]  = userId
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFS] = enabled }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { it.clear() }
    }
}
