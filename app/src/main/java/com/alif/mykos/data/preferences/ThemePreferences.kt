package com.alif.mykos.data.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mykos_preferences")

class ThemePreferences(private val context: Context) {

    companion object {
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")

        const val MODE_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        const val MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO
        const val MODE_DARK = AppCompatDelegate.MODE_NIGHT_YES

        fun applyTheme(mode: Int) {
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    val themeModeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: MODE_SYSTEM
    }

    suspend fun saveThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
        applyTheme(mode)
    }
}