package com.sefa.loldle_karakter.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.IOException

class GameRepository(private val context: Context) {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    fun getGameConfig(gameId: String): GameConfig? {
        val baseName = gameId.split("-").firstOrNull() ?: gameId
        val fileName = "$baseName.json"

        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use {
                it.readText()
            }
            jsonParser.decodeFromString<GameConfig>(jsonString)

        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}