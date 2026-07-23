package com.example.mindquest.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setDifficulty(difficulty: Difficulty)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setDarkTheme(enabled: Boolean)

    /** Data-minimisation control: wipes every stored preference. See also QuizRepository/StatsRepository clear(). */
    suspend fun clearAll()
}

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    override val settings: Flow<UserSettings> = dataStore.data.map { prefs ->
        UserSettings(
            difficulty = prefs[Keys.DIFFICULTY]?.let { name ->
                runCatching { Difficulty.valueOf(name) }.getOrNull()
            } ?: Difficulty.EASY,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            darkTheme = prefs[Keys.DARK_THEME] ?: false
        )
    }

    override suspend fun setDifficulty(difficulty: Difficulty) {
        dataStore.edit { it[Keys.DIFFICULTY] = difficulty.name }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    override suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
