package com.sefa.loldle_karakter.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Calendar

class GameViewModel(
    private val repository: GameRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val gameId: String
) : ViewModel() {

    private var gameConfig: GameConfig? = null
    private var correctEntity: GameEntity? = null
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage
    private val _guessHistory = mutableStateListOf<GuessRow>()
    val guessHistory: List<GuessRow> = _guessHistory
    private val _gameWon = mutableStateOf(false)
    val gameWon: State<Boolean> = _gameWon
    private val _gameLost = mutableStateOf(false)
    val gameLost: State<Boolean> = _gameLost
    private val _correctAnswerName = mutableStateOf("")
    val correctAnswerName: State<String> = _correctAnswerName
    private val _allCharacterNames = mutableStateOf<List<String>>(emptyList())
    val allCharacterNames: State<List<String>> = _allCharacterNames

    init {
        loadGameData()
    }

    private fun loadGameData() {
        gameConfig = repository.getGameConfig(gameId)

        if (gameConfig != null && gameConfig!!.entities.isNotEmpty()) {

            val sortedEntities = gameConfig!!.entities.sortedBy { it.name }
            val listSize = sortedEntities.size
            if (listSize <= 0) {
                _errorMessage.value = "Karakter listesi boş!"
                return
            }

            if (gameId.endsWith("-daily")) {
                val calendar = Calendar.getInstance()
                val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                val year = calendar.get(Calendar.YEAR)
                val dailyIndex = (dayOfYear + year) % listSize
                correctEntity = sortedEntities[dailyIndex]
            } else {
                correctEntity = sortedEntities.random()
            }

            _allCharacterNames.value = sortedEntities.map { it.name }
            _correctAnswerName.value = correctEntity?.name ?: "Hata!"

        } else {
            _errorMessage.value = "Oyun verileri yüklenemedi!"
        }
    }

    fun makeGuess(guessName: String) {
        if (_gameWon.value || _gameLost.value) return
        if (guessName.isBlank()) return

        val guessedEntity = gameConfig?.entities?.find {
            it.name.equals(guessName, ignoreCase = true)
        }

        if (guessedEntity == null) {
            _errorMessage.value = "'$guessName' adında bir karakter bulunamadı."
            return
        }

        val alreadyGuessed = _guessHistory.any {
            it.entityName.equals(guessedEntity.name, ignoreCase = true)
        }
        if (alreadyGuessed) {
            _errorMessage.value = "'${guessedEntity.name}' karakterini zaten tahmin ettin."
            return
        }

        _errorMessage.value = null
        val comparisonResults = compareEntities(guessedEntity, correctEntity!!)

        _guessHistory.add(
            GuessRow(
                entityName = guessedEntity.name,
                imageUrl = guessedEntity.imageUrl,
                comparisons = comparisonResults
            )
        )

        if (guessedEntity.name == correctEntity?.name) {
            _gameWon.value = true

            if (gameId.endsWith("-daily")) {
                markDailyAsPlayed()
            }
        }
    }

    private fun compareEntities(guessed: GameEntity, correct: GameEntity): List<AttributeComparison> {
        val results = mutableListOf<AttributeComparison>()

        gameConfig!!.attributesToCompare.forEach { attributeName ->
            val guessedValueStr = guessed.attributes[attributeName] ?: "Bilinmiyor"
            val correctValueStr = correct.attributes[attributeName] ?: "Bilinmiyor"
            val guessedValues = guessedValueStr.split("|").toSet()
            val correctValues = correctValueStr.split("|").toSet()

            val state = when {
                guessedValues == correctValues -> ComparisonState.CORRECT
                guessedValues.any { it in correctValues } -> ComparisonState.PARTIAL
                else -> ComparisonState.WRONG
            }

            val displayValue = guessedValues.joinToString(", ")
            results.add(AttributeComparison(attributeName, displayValue, state))
        }
        return results
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun giveUp() {
        if (!_gameWon.value) {
            _gameLost.value = true

            if (gameId.endsWith("-daily")) {
                markDailyAsPlayed()
            }
        }
    }

    private fun markDailyAsPlayed() {
        val gameType = gameId.split("-").firstOrNull() ?: return

        viewModelScope.launch {
            prefsRepository.markDailyAsPlayed(gameType)
        }
    }
}

class GameViewModelFactory(
    private val repository: GameRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val gameId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository, prefsRepository, gameId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}