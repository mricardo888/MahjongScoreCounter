package com.ricdev.mahjongscorecounter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "mahjong_state")

object PreferencesKeys {
    val RULES_JSON = stringPreferencesKey("rules_json")
    val HISTORY_JSON = stringPreferencesKey("history_json")
}
