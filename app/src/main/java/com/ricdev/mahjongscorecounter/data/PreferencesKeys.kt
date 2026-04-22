package com.ricdev.mahjongscorecounter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "mahjong_state")

object PreferencesKeys {
    val HISTORY_V1_JSON = stringPreferencesKey("history_v1_json")
    val THEME_MODE = stringPreferencesKey("theme_mode")
}
