package com.ricdev.mahjongscorecounter.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

open class GameRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val historySerializer = ListSerializer(CommittedRound.serializer())

    open val themeModeFlow: Flow<ThemeMode> = dataStore.data
        .catch { throwable ->
            Log.e(TAG, "Failed to read theme preferences", throwable)
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs ->
            val raw = prefs[PreferencesKeys.THEME_MODE] ?: return@map ThemeMode.SYSTEM
            runCatching { ThemeMode.valueOf(raw) }.getOrElse { ThemeMode.SYSTEM }
        }

    open val historyFlow: Flow<List<CommittedRound>> = dataStore.data
        .catch { throwable ->
            Log.e(TAG, "Failed to read history preferences", throwable)
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs ->
            val raw = prefs[PreferencesKeys.HISTORY_V1_JSON] ?: return@map emptyList()
            runCatching { json.decodeFromString(historySerializer, raw) }
                .onFailure { Log.w(TAG, "History decode failed, resetting to empty", it) }
                .getOrElse { emptyList() }
        }

    open suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.THEME_MODE] = mode.name }
    }

    open suspend fun appendRound(round: CommittedRound) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.HISTORY_V1_JSON]?.let { raw ->
                runCatching { json.decodeFromString(historySerializer, raw) }.getOrElse { emptyList() }
            } ?: emptyList()
            val next = (current + round).let { combined ->
                if (combined.size > HISTORY_CAP) combined.takeLast(HISTORY_CAP) else combined
            }
            prefs[PreferencesKeys.HISTORY_V1_JSON] = json.encodeToString(historySerializer, next)
        }
    }

    open suspend fun dropLastRound() {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.HISTORY_V1_JSON]?.let { raw ->
                runCatching { json.decodeFromString(historySerializer, raw) }.getOrElse { emptyList() }
            } ?: emptyList()
            if (current.isEmpty()) return@edit
            val next = current.dropLast(1)
            prefs[PreferencesKeys.HISTORY_V1_JSON] = json.encodeToString(historySerializer, next)
        }
    }

    open suspend fun clearHistory() {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.HISTORY_V1_JSON] = json.encodeToString(historySerializer, emptyList())
        }
    }

    companion object {
        private const val TAG = "GameRepository"
        const val HISTORY_CAP = 500
    }
}
