package com.sefa.loldle_karakter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sefa.loldle_karakter.R
import com.sefa.loldle_karakter.data.MainViewModel
import com.sefa.loldle_karakter.data.UserPreferencesRepository
import kotlinx.coroutines.launch

private enum class PanelState {
    None,
    Games,
    Anime
}

@Composable
fun GameSelectionScreen(
    userPrefsRepository: UserPreferencesRepository,
    onGameSelected: (String) -> Unit,
    mainViewModel: MainViewModel = viewModel()
) {

    val isGamePanelOpen by mainViewModel.isGamePanelOpen
    val isAnimePanelOpen by mainViewModel.isAnimePanelOpen

    val currentPanel = when {
        isGamePanelOpen -> PanelState.Games
        isAnimePanelOpen -> PanelState.Anime
        else -> PanelState.None
    }

    var lolDailyPlayed by remember { mutableStateOf(false) }
    var mlbbDailyPlayed by remember { mutableStateOf(false) }
    var minecraftDailyPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(currentPanel) {
        if (currentPanel == PanelState.Games) {
            lolDailyPlayed = userPrefsRepository.hasPlayedDaily("lol")
            mlbbDailyPlayed = userPrefsRepository.hasPlayedDaily("mlbb")
            minecraftDailyPlayed = userPrefsRepository.hasPlayedDaily("minecraft")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.anaarkaplan),
            contentDescription = "Oyun Seçim Arka Planı",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = {
                onGameSelected("lol-lore")
                mainViewModel.closePanels()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 40.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Filled.MenuBook,
                contentDescription = "LoL Hikâye Kütüphanesi",
                tint = Color.White
            )
        }

        Text(
            text = "GAMEDLE",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 100.dp)
                .fillMaxWidth(0.6f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val mainMenuButtonModifier = Modifier
                .fillMaxWidth()
                .height(50.dp)

            Button(
                onClick = { mainViewModel.openGamePanel() },
                modifier = mainMenuButtonModifier
            ) {
                Text("OYUNLAR")
            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = { mainViewModel.openAnimePanel() },
                modifier = mainMenuButtonModifier
            ) {
                Text("ANİMELER")
            }
        }

        SelectionPanel(
            isVisible = currentPanel == PanelState.Games,
            title = "Oyun Modu Seç",
            onClose = { mainViewModel.closePanels() }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PanelButton(
                    text = "LOLDLE",
                    onClick = {
                        onGameSelected("lol-practice")
                        mainViewModel.closePanels()
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                DailyButton(
                    onClick = {
                        onGameSelected("lol-daily")
                        mainViewModel.closePanels()
                    },
                    isPlayed = lolDailyPlayed
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                PanelButton(
                    text = "MLBBDLE",
                    onClick = {
                        onGameSelected("mlbb-practice")
                        mainViewModel.closePanels()
                    },
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                DailyButton(
                    onClick = {
                        onGameSelected("mlbb-daily")
                        mainViewModel.closePanels()
                    },
                    isPlayed = mlbbDailyPlayed,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                PanelButton(
                    text = "MINECRAFTLE",
                    onClick = {
                        onGameSelected("minecraft")
                        mainViewModel.closePanels()
                    },
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(8.dp))
                DailyButton(
                    onClick = {
                        onGameSelected("minecraft-daily")
                        mainViewModel.closePanels()
                    },
                    isPlayed = minecraftDailyPlayed,
                    color = Color(0xFF4CAF50)
                )
            }
        }

        SelectionPanel(
            isVisible = currentPanel == PanelState.Anime,
            title = "Anime Seç",
            onClose = { mainViewModel.closePanels() }
        ) {
            PanelButton(
                text = "YAKINDA...",
                onClick = { },
                isEnabled = false
            )
        }
    }
}


@Composable
private fun SelectionPanel(
    isVisible: Boolean,
    title: String,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable(enabled = false) { }
                    .padding(24.dp, 32.dp, 24.dp, 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                content()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.85f)
                    .padding(bottom = 300.dp, end = 12.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = "X",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.8f))
                        .clickable(onClick = onClose)
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun PanelButton(
    text: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.8f),
            disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        )
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DailyButton(
    onClick: () -> Unit,
    isPlayed: Boolean,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        enabled = !isPlayed,
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.8f),
            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
        )
    ) {
        if (isPlayed) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Günlük Tamamlandı", tint = Color.White)
        } else {
            Icon(Icons.Default.CalendarToday, contentDescription = "Günlük Mod", tint = Color.White)
        }
    }
}