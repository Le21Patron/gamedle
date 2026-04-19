package com.sefa.loldle_karakter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_prefs")

class UserPreferencesRepository(private val context: Context) {

    private val LAST_PLAYED_LOL_DAILY = stringPreferencesKey("last_played_lol_daily")
    private val LAST_PLAYED_MLBB_DAILY = stringPreferencesKey("last_played_mlbb_daily")
    private val LAST_PLAYED_MINECRAFT_DAILY = stringPreferencesKey("last_played_minecraft_daily")

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun hasPlayedDaily(gameType: String): Boolean {
        val key = when (gameType) {
            "lol" -> LAST_PLAYED_LOL_DAILY
            "mlbb" -> LAST_PLAYED_MLBB_DAILY
            "minecraft" -> LAST_PLAYED_MINECRAFT_DAILY
            else -> LAST_PLAYED_LOL_DAILY
        }
        val today = getTodayDateString()
        val lastPlayedDate = context.dataStore.data.map { preferences ->
            preferences[key] ?: ""
        }.first()
        return lastPlayedDate == today
    }

    suspend fun markDailyAsPlayed(gameType: String) {
        val key = when (gameType) {
            "lol" -> LAST_PLAYED_LOL_DAILY
            "mlbb" -> LAST_PLAYED_MLBB_DAILY
            "minecraft" -> LAST_PLAYED_MINECRAFT_DAILY
            else -> LAST_PLAYED_LOL_DAILY
        }
        val today = getTodayDateString()

        context.dataStore.edit { preferences ->
            preferences[key] = today
        }
    }
}