package com.ricdev.mahjongscorecounter.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
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
        classDiscriminator = "type"
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

    open val rulesFlow: Flow<ScoreRules> = dataStore.data
        .catch { throwable ->
            Log.e(TAG, "Failed to read rules preferences", throwable)
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs ->
            val raw = prefs[PreferencesKeys.RULES_JSON] ?: return@map ScoreRules.HongKongNew()
            runCatching { json.decodeFromString(ScoreRules.serializer(), raw) }
                .onFailure { Log.w(TAG, "Rules decode failed, falling back to HK New", it) }
                .getOrElse { ScoreRules.HongKongNew() }
        }

    open val historyFlow: Flow<List<CommittedRound>> = dataStore.data
        .catch { throwable ->
            Log.e(TAG, "Failed to read history preferences", throwable)
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs ->
            val raw = prefs[PreferencesKeys.HISTORY_JSON] ?: return@map emptyList()
            runCatching { json.decodeFromString(historySerializer, raw) }
                .onFailure { Log.w(TAG, "History decode failed, resetting to empty", it) }
                .getOrElse { emptyList() }
        }

    open val dealerFlow: Flow<Seat> = dataStore.data
        .catch { throwable ->
            Log.e(TAG, "Failed to read dealer preference", throwable)
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs ->
            val raw = prefs[PreferencesKeys.DEALER_SEAT] ?: return@map Seat.EAST
            runCatching { Seat.valueOf(raw) }.getOrElse { Seat.EAST }
        }

    open val honbaFlow: Flow<Int> = dataStore.data
        .catch { throwable ->
            Log.e(TAG, "Failed to read honba preference", throwable)
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs -> prefs[PreferencesKeys.HONBA_COUNT] ?: 0 }

    open val riichiSticksFlow: Flow<Int> = dataStore.data
        .catch { throwable ->
            Log.e(TAG, "Failed to read riichi stick preference", throwable)
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs -> prefs[PreferencesKeys.RIICHI_STICKS] ?: 0 }

    open suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.THEME_MODE] = mode.name }
    }

    open suspend fun updateRules(rules: ScoreRules) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.RULES_JSON] = json.encodeToString(ScoreRules.serializer(), rules)
        }
    }

    open suspend fun updateDealer(seat: Seat) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.DEALER_SEAT] = seat.name }
    }

    open suspend fun updateHonba(count: Int) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.HONBA_COUNT] = count.coerceAtLeast(0) }
    }

    open suspend fun updateRiichiSticks(count: Int) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.RIICHI_STICKS] = count.coerceAtLeast(0) }
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
            prefs[PreferencesKeys.HONBA_COUNT] = 0
            prefs[PreferencesKeys.RIICHI_STICKS] = 0
        }
    }

    companion object {
        private const val TAG = "GameRepository"
        const val HISTORY_CAP = 500
    }
}
