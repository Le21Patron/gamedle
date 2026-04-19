package com.sefa.loldle_karakter.data

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import android.util.Base64
import android.graphics.BitmapFactory
import java.util.Calendar

@Serializable
data class MinecraftItemData(
    val name: String,
    val icon: String,
    val stack: Int
)

@Serializable
data class MinecraftRecipe(
    val targetItem: String,
    val recipe: List<String?>
)

enum class SlotState {
    DEFAULT, CORRECT, WRONG_SPOT, INCORRECT
}

enum class GameState {
    WAITING,
    PLAYING,
    GAME_OVER
}

class MinecraftViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("minecraft_prefs", Context.MODE_PRIVATE)

    private val _recipes = mutableListOf<MinecraftRecipe>()
    private val _itemsData = mutableMapOf<String, MinecraftItemData>()
    private val _bitmapCache = mutableMapOf<String, ImageBitmap>()

    private val _isDataLoaded = mutableStateOf(false)
    val isDataLoaded = _isDataLoaded
    var gameState = mutableStateOf(GameState.WAITING)
    var timeLeft = mutableIntStateOf(30)
    var currentScore = mutableIntStateOf(0)
    var highScore = mutableIntStateOf(0)

    private var timerJob: Job? = null
    private val usedQuestions = mutableListOf<String>()

    private val _currentTarget = mutableStateOf<MinecraftRecipe?>(null)
    val currentTarget = _currentTarget

    val userGrid = mutableStateListOf<String?>().apply { repeat(9) { add(null) } }
    val feedbackGrid = mutableStateListOf<SlotState>().apply { repeat(9) { add(SlotState.DEFAULT) } }

    val shakeTrigger = mutableIntStateOf(0)
    var isDailyMode = mutableStateOf(false)
    private val _dailyTarget = mutableStateOf<MinecraftRecipe?>(null)
    val dailyTarget = _dailyTarget
    private val _correctItems = mutableStateListOf<String>()
    val correctItems: List<String> = _correctItems
    private val _availableInventoryItems = mutableStateListOf<String>()
    val availableInventoryItems: List<String> = _availableInventoryItems
    private val _usedItems = mutableStateListOf<String>()
    val usedItems: List<String> = _usedItems
    private val _dailyGameWon = mutableStateOf(false)
    val dailyGameWon = _dailyGameWon
    private val _usedCorrectMaterials = mutableSetOf<String>()
    private val _usedCorrectMaterialsTrigger = mutableIntStateOf(0)
    val usedCorrectMaterialsTrigger = _usedCorrectMaterialsTrigger
    private val originalInventoryItems = listOf(
        "Tahta", "Kırıktaş", "Taş", "Cam", "Yün", "Çubuk",
        "Kömür", "Elmas", "Altın Külçesi", "Demir Külçesi", "Kızıltaş Tozu", "Kuvars",
        "Tahta Basamak", "Kütük", "Demir Parçası", "Kızıltaş Meşalesi", "İp", "Deri"
    )
    
    val inventoryItems: List<String>
        get() = if (isDailyMode.value) _availableInventoryItems else originalInventoryItems

    private val _selectedItemName = mutableStateOf<String?>(null)
    val selectedItemName = _selectedItemName

    private val _producedItemName = mutableStateOf<String?>(null)
    val producedItemName = _producedItemName

    private val itemIds = mapOf(
        "Tahta" to "minecraft:planks", "Kırıktaş" to "minecraft:cobblestone", "Taş" to "minecraft:stone", "Cam" to "minecraft:glass",
        "Yün" to "minecraft:white_wool", "Çubuk" to "minecraft:stick", "Kömür" to "minecraft:coal", "Elmas" to "minecraft:diamond",
        "Altın Külçesi" to "minecraft:gold_ingot", "Demir Külçesi" to "minecraft:iron_ingot", "Kızıltaş Tozu" to "minecraft:redstone",
        "Kuvars" to "minecraft:quartz", "Kütük" to "minecraft:oak_log", "Demir Parçası" to "minecraft:iron_nugget",
        "İp" to "minecraft:string", "Deri" to "minecraft:leather", "Tahta Basamak" to "minecraft:oak_slab",
        "Elmas Blok" to "minecraft:diamond_block", "Altın Blok" to "minecraft:gold_block", "Demir Blok" to "minecraft:iron_block",
        "Kızıltaş Bloğu" to "minecraft:redstone_block", "Kuvars Bloğu" to "minecraft:quartz_block",
        "Çalışma Masası" to "minecraft:crafting_table", "Fırın" to "minecraft:furnace", "Sandık" to "minecraft:chest",
        "Meşale" to "minecraft:torch", "Kızıltaş Meşalesi" to "minecraft:redstone_torch", "Yatak" to "minecraft:white_bed",
        "Tekne" to "minecraft:oak_boat", "Kova" to "minecraft:bucket", "Makas" to "minecraft:shears", "Yay" to "minecraft:bow",
        "Olta" to "minecraft:fishing_rod", "Pusula" to "minecraft:compass", "Saat" to "minecraft:clock", "Tablo" to "minecraft:painting",
        "Çit" to "minecraft:oak_fence", "Çit Kapısı" to "minecraft:oak_fence_gate", "Kapı" to "minecraft:oak_door",
        "Tuzak Kapısı" to "minecraft:oak_trapdoor", "Merdiven" to "minecraft:oak_stairs", "Tabela" to "minecraft:oak_sign",
        "Şalter" to "minecraft:lever", "Piston" to "minecraft:piston", "Ray" to "minecraft:rail", "Maden Arabası" to "minecraft:minecart",
        "Kazan" to "minecraft:cauldron", "Demir Parmaklık" to "minecraft:iron_bars", "Güçlendirilmiş Ray" to "minecraft:powered_rail",
        "Müzik Bloğu" to "minecraft:note_block", "Kömür Bloğu" to "minecraft:coal_block", "Kamp Ateşi" to "minecraft:campfire",
        "İnce Cam" to "minecraft:glass_pane", "Eşya Çerçevesi" to "minecraft:item_frame", "Kalkan" to "minecraft:shield",
        "Bırakıcı" to "minecraft:dropper", "Gözlemci" to "minecraft:observer", "Varil" to "minecraft:barrel",
        "Demirci Masası" to "minecraft:smithing_table", "Taş Kesici" to "minecraft:stonecutter", "Zincir" to "minecraft:chain",
        "Aktifleyici Ray" to "minecraft:activator_rail", "Kızıltaş Yenileyici" to "minecraft:repeater",
        "Kızıltaş Karşılaştırıcı" to "minecraft:comparator", "Güneş Sensörü" to "minecraft:daylight_detector",
        "Müzik Kutusu" to "minecraft:jukebox", "Cam Şişe" to "minecraft:glass_bottle", "Tuzak Kancası" to "minecraft:tripwire_hook",
        "Taş Düğme" to "minecraft:stone_button", "Tahta Düğme" to "minecraft:oak_button", "Taş Basınç Plakası" to "minecraft:stone_pressure_plate",
        "Tahta Basınç Plakası" to "minecraft:oak_pressure_plate", "Demir Kapı" to "minecraft:iron_door",
        "Demir Tuzak Kapısı" to "minecraft:iron_trapdoor", "Kırıktaş Merdiveni" to "minecraft:cobblestone_stairs",
        "Kırıktaş Duvarı" to "minecraft:cobblestone_wall", "Beyaz Halı" to "minecraft:white_carpet",
        "Dokuma Tezgahı" to "minecraft:loom", "Komposter" to "minecraft:composter", "Taş Tuğla" to "minecraft:stone_bricks",
        "Taş Basamak" to "minecraft:stone_slab",
        "Ağır Basınç Plakası" to "minecraft:heavy_weighted_pressure_plate", "Hafif Basınç Plakası" to "minecraft:light_weighted_pressure_plate",
        "Tahta Kılıç" to "minecraft:wooden_sword", "Tahta Kazma" to "minecraft:wooden_pickaxe", "Tahta Balta" to "minecraft:wooden_axe",
        "Tahta Kürek" to "minecraft:wooden_shovel", "Tahta Çapa" to "minecraft:wooden_hoe", "Taş Kılıç" to "minecraft:stone_sword",
        "Taş Kazma" to "minecraft:stone_pickaxe", "Taş Balta" to "minecraft:stone_axe", "Taş Kürek" to "minecraft:stone_shovel",
        "Taş Çapa" to "minecraft:stone_hoe", "Demir Kılıç" to "minecraft:iron_sword", "Demir Kazma" to "minecraft:iron_pickaxe",
        "Demir Balta" to "minecraft:iron_axe", "Demir Kürek" to "minecraft:iron_shovel", "Demir Çapa" to "minecraft:iron_hoe",
        "Altın Kılıç" to "minecraft:golden_sword", "Altın Kazma" to "minecraft:golden_pickaxe", "Altın Balta" to "minecraft:golden_axe",
        "Altın Kürek" to "minecraft:golden_shovel", "Altın Çapa" to "minecraft:golden_hoe", "Elmas Kılıç" to "minecraft:diamond_sword",
        "Elmas Kazma" to "minecraft:diamond_pickaxe", "Elmas Balta" to "minecraft:diamond_axe", "Elmas Kürek" to "minecraft:diamond_shovel",
        "Elmas Çapa" to "minecraft:diamond_hoe", "Deri Başlık" to "minecraft:leather_helmet", "Deri Göğüslük" to "minecraft:leather_chestplate",
        "Deri Pantolon" to "minecraft:leather_leggings", "Deri Bot" to "minecraft:leather_boots", "Demir Miğfer" to "minecraft:iron_helmet",
        "Demir Göğüslük" to "minecraft:iron_chestplate", "Demir Pantolon" to "minecraft:iron_leggings", "Demir Bot" to "minecraft:iron_boots",
        "Altın Miğfer" to "minecraft:golden_helmet", "Altın Göğüslük" to "minecraft:golden_chestplate", "Altın Pantolon" to "minecraft:golden_leggings",
        "Altın Bot" to "minecraft:golden_boots", "Elmas Miğfer" to "minecraft:diamond_helmet", "Elmas Göğüslük" to "minecraft:diamond_chestplate",
        "Elmas Pantolon" to "minecraft:diamond_leggings", "Elmas Bot" to "minecraft:diamond_boots"
    )

    init {
        highScore.intValue = prefs.getInt("high_score", 0)
        viewModelScope.launch {
            loadDataAsync()
        }
    }

    private suspend fun loadDataAsync() {
        withContext(Dispatchers.IO) {
            try {
                val itemsInputStream = context.assets.open("items.json")
                val itemsReader = InputStreamReader(itemsInputStream)
                val itemsJsonString = itemsReader.readText()
                val itemsMap = Json { ignoreUnknownKeys = true }.decodeFromString<Map<String, MinecraftItemData>>(itemsJsonString)
                _itemsData.putAll(itemsMap)
                
                val recipesInputStream = context.assets.open("minecraft.json")
                val recipesReader = InputStreamReader(recipesInputStream)
                val recipesJsonString = recipesReader.readText()
                val list = Json.decodeFromString<List<MinecraftRecipe>>(recipesJsonString)
                _recipes.addAll(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (_recipes.isNotEmpty()) {
            prepareNextQuestion()
        }
        _isDataLoaded.value = true
    }

    fun startGame() {
        if (!_isDataLoaded.value || _recipes.isEmpty()) return
        
        isDailyMode.value = false
        currentScore.intValue = 0
        timeLeft.intValue = 30
        usedQuestions.clear()
        gameState.value = GameState.PLAYING
        prepareNextQuestion()
        startTimer()
    }

    fun startDailyGame() {
        if (!_isDataLoaded.value || _recipes.isEmpty()) return
        
        isDailyMode.value = true
        _correctItems.clear()
        _usedItems.clear()
        _usedCorrectMaterials.clear()
        _usedCorrectMaterialsTrigger.intValue = 0
        _dailyGameWon.value = false
        _availableInventoryItems.clear()
        _availableInventoryItems.addAll(originalInventoryItems)
        clearGrid()
        
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val dailyIndex = (dayOfYear + year) % _recipes.size
        _dailyTarget.value = _recipes[dailyIndex]
        _currentTarget.value = _dailyTarget.value
        preloadDailyBitmaps()
        
        gameState.value = GameState.PLAYING
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timeLeft.intValue > 0 && gameState.value == GameState.PLAYING) {
                delay(1000L)
                timeLeft.intValue -= 1
            }
            if (timeLeft.intValue == 0) {
                endGame()
            }
        }
    }

    private fun endGame() {
        gameState.value = GameState.GAME_OVER
        if (currentScore.intValue > highScore.intValue) {
            highScore.intValue = currentScore.intValue
            prefs.edit().putInt("high_score", highScore.intValue).apply()
        }
    }

    private fun prepareNextQuestion() {
        if (_recipes.isNotEmpty()) {
            val availableRecipes = _recipes.filter { !usedQuestions.contains(it.targetItem) }

            val nextRecipe = if (availableRecipes.isNotEmpty()) {
                availableRecipes.random()
            } else {
                usedQuestions.clear()
                _recipes.random()
            }

            usedQuestions.add(nextRecipe.targetItem)
            _currentTarget.value = nextRecipe
            clearGrid()
        }
    }

    fun selectItemFromInventory(itemName: String) {
        if (gameState.value != GameState.PLAYING) return
        if (_selectedItemName.value == itemName) {
            _selectedItemName.value = null
        } else {
            _selectedItemName.value = itemName
        }
    }

    fun placeSelectedItemInSlot(slotIndex: Int) {
        if (gameState.value != GameState.PLAYING) return

        if (userGrid[slotIndex] != null) {
            userGrid[slotIndex] = null
            resetFeedback()
            if (isDailyMode.value) {
                _producedItemName.value = null
            }
            checkIfRecipeIsValid()
            return
        }
        val selectedItem = _selectedItemName.value
        if (selectedItem != null) {
            userGrid[slotIndex] = selectedItem
            resetFeedback()
            if (isDailyMode.value) {
                _producedItemName.value = null
            }
            checkIfRecipeIsValid()
        }
    }

    private fun checkIfRecipeIsValid() {
        val matchedRecipe = _recipes.find { recipe ->
            isRecipeMatch(userGrid, recipe.recipe)
        }
        if (matchedRecipe != null) {
            _producedItemName.value = matchedRecipe.targetItem

            if (!isDailyMode.value) {
                val target = _currentTarget.value
                if (target != null && matchedRecipe.targetItem == target.targetItem) {
                    handleCorrectAnswer()
                }
            }
        } else {
            _producedItemName.value = null
        }
    }
    
    fun checkDailyCraft() {
        if (!isDailyMode.value || _producedItemName.value == null) return
        
        val dailyTarget = _dailyTarget.value ?: return
        val producedItem = _producedItemName.value ?: return
        
        if (producedItem == dailyTarget.targetItem) {
            handleDailyWin()
            return
        }
        
        val targetRecipeItems = dailyTarget.recipe.filterNotNull().toSet()
        val usedMaterials = userGrid.filterNotNull().toSet()
        val correctUsedMaterials = usedMaterials.filter { it in targetRecipeItems }
        _usedCorrectMaterials.addAll(correctUsedMaterials)
        _usedCorrectMaterialsTrigger.intValue++
        val wrongMaterials = usedMaterials.filter { it !in targetRecipeItems }
        _availableInventoryItems.removeAll(wrongMaterials)
        
        if (!_usedItems.contains(producedItem)) {
            _usedItems.add(producedItem)
        }
        
        clearGrid()
    }
    
    fun isMaterialUsedAndCorrect(materialName: String): Boolean {
        if (!isDailyMode.value) return false
        return materialName in _usedCorrectMaterials
    }
    


    private fun handleDailyWin() {
        _dailyGameWon.value = true
        gameState.value = GameState.GAME_OVER
    }
    
    fun giveUpDaily() {
        if (!isDailyMode.value) return
        _dailyGameWon.value = false
        gameState.value = GameState.GAME_OVER
    }

    private fun handleCorrectAnswer() {
        currentScore.intValue += 1
        prepareNextQuestion()
    }

    fun clearGrid() {
        for (i in 0 until userGrid.size) {
            userGrid[i] = null
        }
        resetFeedback()
        _selectedItemName.value = null
        _producedItemName.value = null
    }

    private fun extractPattern(grid: List<String?>): List<List<String>> {
        val matrix = grid.chunked(3)
        var minRow = 3
        var maxRow = -1
        var minCol = 3
        var maxCol = -1

        for (r in 0 until 3) {
            for (c in 0 until 3) {
                if (matrix[r][c] != null) {
                    if (r < minRow) minRow = r
                    if (r > maxRow) maxRow = r
                    if (c < minCol) minCol = c
                    if (c > maxCol) maxCol = c
                }
            }
        }

        if (maxRow == -1) return emptyList()

        val pattern = mutableListOf<List<String>>()
        for (r in minRow..maxRow) {
            val rowList = mutableListOf<String>()
            for (c in minCol..maxCol) {
                rowList.add(matrix[r][c] ?: "AIR")
            }
            pattern.add(rowList)
        }
        return pattern
    }

    private fun mirrorPattern(pattern: List<List<String>>): List<List<String>> {
        return pattern.map { row -> row.reversed() }
    }

    private fun isRecipeMatch(userGrid: List<String?>, targetRecipe: List<String?>): Boolean {
        val userPattern = extractPattern(userGrid)
        val targetPattern = extractPattern(targetRecipe)
        if (userPattern.isEmpty()) return false
        if (userPattern == targetPattern) return true
        val mirroredTarget = mirrorPattern(targetPattern)
        if (userPattern == mirroredTarget) return true
        return false
    }

    fun getItemImageBitmap(itemName: String): ImageBitmap? {
        val id = itemIds[itemName] ?: return null
        if (_bitmapCache.containsKey(id)) {
            return _bitmapCache[id]
        }
        val itemData = _itemsData[id] ?: return null
        
        return try {
            val cleanBase64 = itemData.icon.substringAfter("base64,")
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size).asImageBitmap()
            _bitmapCache[id] = bitmap
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun preloadDailyBitmaps() {
        viewModelScope.launch(Dispatchers.Default) {
            originalInventoryItems.forEach { itemName ->
                val id = itemIds[itemName] ?: return@forEach
                if (!_bitmapCache.containsKey(id)) {
                    val itemData = _itemsData[id] ?: return@forEach
                    try {
                        val cleanBase64 = itemData.icon.substringAfter("base64,")
                        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size).asImageBitmap()
                        _bitmapCache[id] = bitmap
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    private fun resetFeedback() {
        for(i in 0 until 9) feedbackGrid[i] = SlotState.DEFAULT
    }
}