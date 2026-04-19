package com.sefa.loldle_karakter.data

import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
    val gameId: String,
    val gameName: String,
    val attributesToCompare: List<String>,
    val entities: List<GameEntity>
)

@Serializable
data class GameEntity(
    val name: String,
    val imageUrl: String,
    val attributes: Map<String, String>
)