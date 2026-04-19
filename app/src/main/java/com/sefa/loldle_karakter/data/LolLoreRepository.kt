package com.sefa.loldle_karakter.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

@Serializable
data class DDragonChampionListResponse(
    val data: Map<String, DDragonChampionSummaryDto>
)

@Serializable
data class DDragonChampionSummaryDto(
    val id: String,
    val name: String,
    val title: String,
    val blurb: String? = null,
    val tags: List<String> = emptyList(),
    val image: DDragonImageDto
)

@Serializable
data class DDragonImageDto(
    val full: String
)

@Serializable
data class DDragonChampionDetailResponse(
    val data: Map<String, DDragonChampionDetailDto>
)

@Serializable
data class DDragonChampionDetailDto(
    val id: String,
    val name: String,
    val title: String,
    val lore: String? = null,
    @SerialName("allytips")
    val allyTips: List<String> = emptyList(),
    @SerialName("enemytips")
    val enemyTips: List<String> = emptyList()
)

data class ChampionSummary(
    val id: String,
    val name: String,
    val title: String,
    val blurb: String,
    val tags: List<String>,
    val iconUrl: String
)

data class ChampionDetail(
    val id: String,
    val name: String,
    val title: String,
    val lore: String,
    val allyTips: List<String>,
    val enemyTips: List<String>
)

class LolLoreRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private var cachedVersion: String? = null

    private suspend fun getLatestVersion(): String {
        cachedVersion?.let { return it }

        val body = httpGet("https://ddragon.leagueoflegends.com/api/versions.json")
        val versions = json.decodeFromString<List<String>>(body)
        val latest = versions.firstOrNull()
            ?: throw IllegalStateException("Data Dragon sürüm listesi boş geldi")
        cachedVersion = latest
        return latest
    }

    private fun resolveLanguage(): String {
        val default = Locale.getDefault()
        return when (default.language.lowercase(Locale.ROOT)) {
            "tr" -> "tr_TR"
            else -> "en_US"
        }
    }

    private suspend fun httpGet(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.requestMethod = "GET"
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val body = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) {
                throw IllegalStateException("Sunucu hatası ($code)")
            }
            body
        } finally {
            connection.disconnect()
        }
    }

    suspend fun fetchChampionList(): List<ChampionSummary> {
        val version = getLatestVersion()
        val lang = resolveLanguage()
        val url = "https://ddragon.leagueoflegends.com/cdn/$version/data/$lang/champion.json"

        val body = httpGet(url)
        val dto = json.decodeFromString<DDragonChampionListResponse>(body)

        return dto.data.values
            .sortedBy { it.name }
            .map { champ ->
                ChampionSummary(
                    id = champ.id,
                    name = champ.name,
                    title = champ.title,
                    blurb = champ.blurb.orEmpty(),
                    tags = champ.tags,
                    iconUrl = "https://ddragon.leagueoflegends.com/cdn/$version/img/champion/${champ.image.full}"
                )
            }
    }

    suspend fun fetchChampionDetail(championId: String): ChampionDetail {
        val version = getLatestVersion()
        val lang = resolveLanguage()
        val url =
            "https://ddragon.leagueoflegends.com/cdn/$version/data/$lang/champion/$championId.json"

        val body = httpGet(url)
        val dto = json.decodeFromString<DDragonChampionDetailResponse>(body)
        val detailDto = dto.data.values.firstOrNull()
            ?: throw IllegalStateException("Şampiyon detayı bulunamadı")

        return ChampionDetail(
            id = detailDto.id,
            name = detailDto.name,
            title = detailDto.title,
            lore = detailDto.lore.orEmpty(),
            allyTips = detailDto.allyTips,
            enemyTips = detailDto.enemyTips
        )
    }
}

