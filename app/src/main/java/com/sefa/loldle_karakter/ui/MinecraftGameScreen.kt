package com.sefa.loldle_karakter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sefa.loldle_karakter.data.GameState
import com.sefa.loldle_karakter.data.MinecraftViewModel
import com.sefa.loldle_karakter.data.SlotState
import com.sefa.loldle_karakter.data.UserPreferencesRepository
import kotlinx.coroutines.launch

val McPanelBg = Color(0xFFC6C6C6)
val McSlotBg = Color(0xFF8B8B8B)
val McShadow = Color(0xFF373737)
val McHighlight = Color(0xFFFFFFFF)
val McText = Color(0xFF3F3F3F)
val McBackground = Color(0xFF121212)
fun Modifier.minecraftBorder(
    width: Float = 4f,
    isSunken: Boolean = false
): Modifier = this.drawBehind {
    val w = size.width
    val h = size.height
    val stroke = width
    val topLeftColor = if (isSunken) McShadow else McHighlight
    val bottomRightColor = if (isSunken) McHighlight else McShadow
    drawLine(color = topLeftColor, start = Offset(0f, h), end = Offset(0f, 0f), strokeWidth = stroke)
    drawLine(color = topLeftColor, start = Offset(0f, 0f), end = Offset(w, 0f), strokeWidth = stroke)
    drawLine(color = bottomRightColor, start = Offset(0f, h), end = Offset(w, h), strokeWidth = stroke)
    drawLine(color = bottomRightColor, start = Offset(w, 0f), end = Offset(w, h), strokeWidth = stroke)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinecraftGameScreen(
    onNavigateBack: () -> Unit,
    isDailyMode: Boolean = false,
    userPrefsRepository: UserPreferencesRepository? = null,
    viewModel: MinecraftViewModel = viewModel()
) {
    val targetRecipe = viewModel.currentTarget.value
    val userGrid = viewModel.userGrid
    val selectedItemName = viewModel.selectedItemName.value
    val producedItemName = viewModel.producedItemName.value

    val gameState = viewModel.gameState.value
    val timeLeft = viewModel.timeLeft.intValue
    val currentScore = viewModel.currentScore.intValue
    val highScore = viewModel.highScore.intValue
    val isDaily = viewModel.isDailyMode.value
    val correctItems = viewModel.correctItems
    val availableInventory = viewModel.availableInventoryItems
    val usedItems = viewModel.usedItems
    val isDataLoaded = viewModel.isDataLoaded.value
    val dailyGameWon = viewModel.dailyGameWon.value
    val usedCorrectMaterialsTrigger = viewModel.usedCorrectMaterialsTrigger.intValue

    val shakeTrigger = viewModel.shakeTrigger.intValue
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isDailyMode, viewModel.isDataLoaded.value) {
        if (isDailyMode && gameState == GameState.WAITING && viewModel.isDataLoaded.value) {
            viewModel.startDailyGame()
        }
    }

    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 500
                    0f at 0
                    (-8f) at 50
                    8f at 100
                    (-8f) at 150
                    8f at 200
                    (-5f) at 250
                    5f at 300
                    0f at 500 using androidx.compose.animation.core.LinearEasing
                }
            )
        }
    }

    Scaffold(
        topBar = {
            if (gameState == GameState.PLAYING && !isDaily) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PUAN", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("$currentScore", fontSize = 20.sp, color = Color.Green, fontWeight = FontWeight.Bold)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if(timeLeft < 10) "00:0$timeLeft" else "00:$timeLeft",
                                    fontSize = 28.sp,
                                    color = if(timeLeft <= 5) Color.Red else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("REKOR: $highScore", fontSize = 10.sp, color = Color.Yellow)
                            }

                            IconButton(onClick = { showExitDialog = true }) {
                                Icon(Icons.Filled.Close, contentDescription = "Çıkış", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                )
            } else if (gameState == GameState.PLAYING && isDaily) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.giveUpDaily()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.Red
                                )
                            ) {
                                Text("PES ET", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            
                            IconButton(onClick = { showExitDialog = true }) {
                                Icon(Icons.Filled.Close, contentDescription = "Çıkış", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                )
            }
        },
        containerColor = McBackground
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            when (gameState) {
                GameState.PLAYING -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!isDaily) {
                            val targetName = targetRecipe?.targetItem ?: "?"
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text("HEDEF", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val targetBitmap = viewModel.getItemImageBitmap(targetName)
                                    if (targetBitmap != null) {
                                        Image(
                                            bitmap = targetBitmap,
                                            contentDescription = targetName,
                                            modifier = Modifier.size(40.dp),
                                            filterQuality = FilterQuality.None
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    Text(targetName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text("GÜNLÜK MEYDAN OKUMA", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Hedef eşyayı bul!", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer { translationX = shakeOffset.value * density.density }
                                    .background(McPanelBg)
                                    .minecraftBorder(width = 6f, isSunken = false)
                                    .padding(16.dp)
                            ) {

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        for (row in 0 until 3) {
                                            Row {
                                                for (col in 0 until 3) {
                                                    val index = row * 3 + col
                                                    val itemInSlot = if (index < userGrid.size) userGrid[index] else null
                                                    MinecraftSlot(
                                                        itemBitmap = if (itemInSlot != null) viewModel.getItemImageBitmap(itemInSlot) else null,
                                                        slotState = SlotState.DEFAULT,
                                                        onClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            viewModel.placeSelectedItemInSlot(index)
                                                        }
                                                    )
                                                    if (col < 2) Spacer(modifier = Modifier.width(4.dp))
                                                }
                                            }
                                            if (row < 2) Spacer(modifier = Modifier.height(4.dp))
                                        }
                                    }

                                    Text("➔", fontSize = 32.sp, color = McText, modifier = Modifier.padding(horizontal = 14.dp), fontWeight = FontWeight.Black)

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        MinecraftSlot(
                                            itemBitmap = if (producedItemName != null) viewModel.getItemImageBitmap(producedItemName) else null,
                                            isBig = true,
                                            isGreenBorder = producedItemName != null,
                                            onClick = {
                                                if (isDaily && producedItemName != null) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    viewModel.checkDailyCraft()
                                                }
                                            }
                                        )
                                    }
                                }

                                Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp)) {
                                    MinecraftCloseButton(onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.clearGrid()
                                    })
                                }
                            }

                            if (isDaily && correctItems.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-8).dp, y = 8.dp)
                                        .background(McPanelBg)
                                        .minecraftBorder(width = 4f, isSunken = false)
                                        .padding(8.dp)
                                        .width(120.dp)
                                ) {
                                    Column {
                                        Text(
                                            "Doğru Eşyalar",
                                            fontSize = 10.sp,
                                            color = McText,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(3),
                                            verticalArrangement = Arrangement.spacedBy(2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            items(correctItems) { itemName ->
                                                MinecraftSlot(
                                                    itemBitmap = viewModel.getItemImageBitmap(itemName),
                                                    modifier = Modifier.size(32.dp),
                                                    onClick = {}
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isDaily && usedItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Kullanılmış Ürünler",
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(McPanelBg)
                                    .minecraftBorder(width = 4f, isSunken = false)
                                    .padding(8.dp)
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(6),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(usedItems) { itemName ->
                                        MinecraftSlot(
                                            itemBitmap = viewModel.getItemImageBitmap(itemName),
                                            modifier = Modifier.size(40.dp),
                                            onClick = {}
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text("Envanter", color = Color.Gray, modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(McPanelBg)
                                .minecraftBorder(width = 6f, isSunken = false)
                                .padding(10.dp)
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(6),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(if (isDaily) availableInventory else viewModel.inventoryItems) { itemName ->
                                    val isSelected = (itemName == selectedItemName)
                                    val isUsedAndCorrect = if (isDaily) viewModel.isMaterialUsedAndCorrect(itemName) else false
                                    MinecraftSlot(
                                        itemBitmap = viewModel.getItemImageBitmap(itemName),
                                        isSelected = isSelected,
                                        isGreenBorder = isUsedAndCorrect,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.selectItemFromInventory(itemName)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }

                GameState.WAITING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(McPanelBg)
                                .minecraftBorder(width = 6f, isSunken = false)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MINECRAFTLE", fontSize = 36.sp, color = McText, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(24.dp))

                                if (!isDataLoaded) {
                                    Text("Yükleniyor...", fontSize = 16.sp, color = McText)
                                } else {
                                    Text("Meydan Okuma Başlasın!", fontSize = 16.sp, color = McText)
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                Text("REKOR: $highScore", fontSize = 22.sp, color = Color(0xFFAA0000), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(40.dp))

                                MinecraftButton(text = "OYUNA BAŞLA") {
                                    if (isDataLoaded) {
                                        viewModel.startGame()
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                MinecraftButton(text = "ANA MENÜ", isSecondary = true) {
                                    onNavigateBack()
                                }
                            }
                        }
                    }
                }

                GameState.GAME_OVER -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(McPanelBg)
                                .minecraftBorder(width = 6f, isSunken = false)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isDaily) {
                                    if (dailyGameWon) {
                                        Text("TEBRİKLER!", fontSize = 32.sp, color = McText, fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.height(20.dp))
                                        val dailyTargetName = viewModel.dailyTarget.value?.targetItem ?: "?"
                                        Text("Hedef Eşya:", fontSize = 18.sp, color = McText)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val targetBitmap = viewModel.getItemImageBitmap(dailyTargetName)
                                            if (targetBitmap != null) {
                                                Image(
                                                    bitmap = targetBitmap,
                                                    contentDescription = dailyTargetName,
                                                    modifier = Modifier.size(48.dp),
                                                    filterQuality = FilterQuality.None
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                            }
                                            Text(dailyTargetName, fontSize = 24.sp, color = Color(0xFF00AA00), fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text("BAŞARAMADINIZ", fontSize = 32.sp, color = Color(0xFFAA0000), fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Text("Cevap:", fontSize = 18.sp, color = McText)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val dailyTargetName = viewModel.dailyTarget.value?.targetItem ?: "?"
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val targetBitmap = viewModel.getItemImageBitmap(dailyTargetName)
                                            if (targetBitmap != null) {
                                                Image(
                                                    bitmap = targetBitmap,
                                                    contentDescription = dailyTargetName,
                                                    modifier = Modifier.size(48.dp),
                                                    filterQuality = FilterQuality.None
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                            }
                                            Text(dailyTargetName, fontSize = 24.sp, color = Color(0xFFAA0000), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    LaunchedEffect(Unit) {
                                        userPrefsRepository?.markDailyAsPlayed("minecraft")
                                    }
                                } else {
                                    Text("SÜRE BİTTİ!", fontSize = 32.sp, color = McText, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text("SKORUNUZ", fontSize = 18.sp, color = McText)
                                    Text("$currentScore", fontSize = 56.sp, color = Color(0xFF00AA00), fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("REKOR: $highScore", fontSize = 16.sp, color = McText)
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                if (!isDaily) {
                                    MinecraftButton(text = "TEKRAR OYNA") {
                                        viewModel.startGame()
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                MinecraftButton(text = "ANA MENÜ", isSecondary = true) {
                                    onNavigateBack()
                                }
                            }
                        }
                    }
                }
            }

            if (showExitDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(300.dp)
                            .background(McPanelBg)
                            .minecraftBorder(width = 6f, isSunken = false)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "OYUNDAN ÇIK?",
                                fontSize = 20.sp,
                                color = McText,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "İlerlemeniz kaybolacak.",
                                fontSize = 14.sp,
                                color = McText
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(40.dp)
                                        .background(McSlotBg)
                                        .minecraftBorder(width = 3f, isSunken = false)
                                        .clickable { onNavigateBack() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("EVET", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(40.dp)
                                        .background(McSlotBg)
                                        .minecraftBorder(width = 3f, isSunken = false)
                                        .clickable { showExitDialog = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("HAYIR", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MinecraftButton(
    text: String,
    isSecondary: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .width(220.dp)
            .height(50.dp)
            .background(if(isSecondary) Color(0xFF777777) else McSlotBg)
            .minecraftBorder(width = 4f, isSunken = isPressed)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = if(isPressed) 2.dp else 0.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun MinecraftCloseButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val contentOffset by animateDpAsState(if (isPressed) 2.dp else 0.dp, label = "pressOffset")

    Box(
        modifier = Modifier
            .size(26.dp)
            .background(McPanelBg)
            .minecraftBorder(width = 3f, isSunken = isPressed)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("X", color = Color(0xFFCC0000), fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.offset(y = contentOffset))
    }
}

@Composable
fun MinecraftSlot(
    itemBitmap: ImageBitmap?,
    modifier: Modifier = Modifier,
    isBig: Boolean = false,
    isSelected: Boolean = false,
    isGreenBorder: Boolean = false,
    slotState: SlotState = SlotState.DEFAULT,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "pressScale")
    val size = if (isBig) 64.dp else 48.dp

    val stateBorderColor = if (isGreenBorder) Color(0xFF00AA00) else null
    val selectionColor = if (isSelected) Color.White else Color.Transparent

    val itemScale = remember { Animatable(0.5f) }
    LaunchedEffect(itemBitmap) {
        if (itemBitmap != null) {
            itemScale.snapTo(0.5f)
            itemScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
            )
        }
    }

    Box(
        modifier = modifier
            .scale(pressScale)
            .size(size)
            .background(McSlotBg)
            .minecraftBorder(width = 3f, isSunken = true)
            .then(
                if (stateBorderColor != null) Modifier.border(3.dp, stateBorderColor)
                else if (isSelected) Modifier.border(3.dp, selectionColor)
                else Modifier
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = itemBitmap,
            label = "ItemPop",
            transitionSpec = {
                (scaleIn(animationSpec = spring(stiffness = 400f, dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                        (scaleOut(animationSpec = spring(stiffness = 400f)) + fadeOut())
            }
        ) { targetImage: ImageBitmap? ->
            if (targetImage != null) {
                Image(
                    bitmap = targetImage,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(if (isBig) 12.dp else 8.dp)
                        .fillMaxSize(),
                    filterQuality = FilterQuality.None
                )
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}