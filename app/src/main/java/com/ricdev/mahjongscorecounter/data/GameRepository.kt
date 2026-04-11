package com.ricdev.mahjongscorecounter.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.ScoreRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

open class GameRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val historySerializer = ListSerializer(CommittedRound.serializer())

    open val rulesFlow: Flow<ScoreRules> = dataStore.data
        .catch { throwable ->
            Log.e(TAG, "Failed to read rules preferences", throwable)
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs ->
            val raw = prefs[PreferencesKeys.RULES_JSON] ?: return@map ScoreRules()
            runCatching { json.decodeFromString(ScoreRules.serializer(), raw) }
                .onFailure { Log.e(TAG, "Failed to decode rules JSON", it) }
                .getOrElse { ScoreRules() }
        }

    open val historyFlow: Flow<List<CommittedRound>> = dataStore.data
        .catch { throwable ->
            Log.e(TAG, "Failed to read history preferences", throwable)
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs ->
            val raw = prefs[PreferencesKeys.HISTORY_JSON] ?: return@map emptyList()
            runCatching { json.decodeFromString(historySerializer, raw) }
                .onFailure { Log.e(TAG, "Failed to decode history JSON", it) }
                .getOrElse { emptyList() }
        }

    open suspend fun updateRules(rules: ScoreRules) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.RULES_JSON] = json.encodeToString(ScoreRules.serializer(), rules)
        }
    }

    open suspend fun appendRound(round: CommittedRound) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.HISTORY_JSON]?.let { raw ->
                runCatching { json.decodeFromString(historySerializer, raw) }.getOrElse { emptyList() }
            } ?: emptyList()
            val next = (current + round).let { combined ->
                if (combined.size > HISTORY_CAP) combined.takeLast(HISTORY_CAP) else combined
            }
            prefs[PreferencesKeys.HISTORY_JSON] = json.encodeToString(historySerializer, next)
        }
    }

    open suspend fun dropLastRound() {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.HISTORY_JSON]?.let { raw ->
                runCatching { json.decodeFromString(historySerializer, raw) }.getOrElse { emptyList() }
            } ?: emptyList()
            if (current.isEmpty()) return@edit
            val next = current.dropLast(1)
            prefs[PreferencesKeys.HISTORY_JSON] = json.encodeToString(historySerializer, next)
        }
    }

    open suspend fun clearHistory() {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.HISTORY_JSON] = json.encodeToString(historySerializer, emptyList())
        }
    }

    companion object {
        private const val TAG = "GameRepository"
        const val HISTORY_CAP = 500
    }
}
