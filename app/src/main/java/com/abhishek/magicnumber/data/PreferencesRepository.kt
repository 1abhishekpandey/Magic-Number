package com.abhishek.magicnumber.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.abhishek.magicnumber.model.NumberLayout
import com.abhishek.magicnumber.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for managing app preferences using DataStore.
 *
 * Provides reactive access to settings via Flow and suspend functions for updates.
 */
class PreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val MAX_NUMBER = intPreferencesKey("max_number")
        val NUMBER_LAYOUT = stringPreferencesKey("number_layout")
    }

    /**
     * Flow of current settings. Emits whenever settings change.
     */
    val settingsFlow: Flow<Settings> = context.dataStore.data.map { preferences ->
        Settings(
            maxNumber = preferences[PreferencesKeys.MAX_NUMBER] ?: 63,
            numberLayout = preferences[PreferencesKeys.NUMBER_LAYOUT]
                ?.let { NumberLayout.valueOf(it) }
                ?: NumberLayout.ASCENDING
        )
    }

    /**
     * Updates the maximum number setting.
     *
     * @param value The new max number (31, 63, or 127)
     */
    suspend fun updateMaxNumber(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_NUMBER] = value
        }
    }

    /**
     * Updates the number layout setting.
     *
     * @param layout The new layout style
     */
    suspend fun updateNumberLayout(layout: NumberLayout) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NUMBER_LAYOUT] = layout.name
        }
    }
}
