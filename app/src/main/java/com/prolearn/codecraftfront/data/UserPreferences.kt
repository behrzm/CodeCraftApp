package com.prolearn.codecraftfront.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(name = "codequest_prefs")

data class UserPreferences(
    val darkTheme: Boolean = true,
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val DarkTheme = booleanPreferencesKey("dark_theme")
        val Sound = booleanPreferencesKey("sound_enabled")
        val Notifications = booleanPreferencesKey("notifications_enabled")
    }

    val preferences: Flow<UserPreferences> =
        context.userPreferencesDataStore.data.map { prefs ->
            UserPreferences(
                darkTheme = prefs[Keys.DarkTheme] ?: true,
                soundEnabled = prefs[Keys.Sound] ?: true,
                notificationsEnabled = prefs[Keys.Notifications] ?: true,
            )
        }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.userPreferencesDataStore.edit { it[Keys.DarkTheme] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { it[Keys.Sound] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { it[Keys.Notifications] = enabled }
    }
}
