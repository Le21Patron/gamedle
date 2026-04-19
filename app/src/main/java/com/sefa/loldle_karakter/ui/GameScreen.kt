package com.sefa.loldle_karakter.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sefa.loldle_karakter.R
import com.sefa.loldle_karakter.data.GameViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun GameScreen(
    gameId: String,
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val errorMessage by viewModel.errorMessage
    val gameWon by viewModel.gameWon
    val gameLost by viewModel.gameLost
    val correctAnswerName by viewModel.correctAnswerName
    val guessCount = viewModel.guessHistory.size

    val allCharacterNames by viewModel.allCharacterNames
    var textInput by remember { mutableStateOf("") }
    val filteredList by remember(textInput, allCharacterNames) {
        derivedStateOf {
            if (textInput.isBlank()) {
                emptyList()
            } else {
                allCharacterNames.filter {
                    it.startsWith(textInput, ignoreCase = true) &&
                            !it.equals(textInput, ignoreCase = true)
                }
            }
        }
    }

    val backgroundImageRes = when(gameId) {
        "lol" -> R.drawable.lolarkaplan
        "mlbb" -> R.drawable.mlbbarkaplan
        else -> R.drawable.anaarkaplan
    }

    val listState = rememberLazyListState()

    LaunchedEffect(guessCount) {
        if (guessCount > 0) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val gameName = if (gameId.startsWith("lol")) "LOLDLE" else "MLBBDLE"
                    val modeName = if (gameId.endsWith("-daily")) "(Günlük)" else "(Alıştırma)"
                    Text(text = "$gameName $modeName")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri dön")
                    }
                },
                actions = {
                    if (!gameWon && !gameLost) {
                        val GUESS_LIMIT = 5
                        val isButtonEnabled = guessCount >= GUESS_LIMIT
                        val remainingGuesses = (GUESS_LIMIT - guessCount).coerceAtLeast(0)

                        val buttonText = if (guessCount < GUESS_LIMIT) {
                            remainingGuesses.toString()
                        } else {
                            "PES ET"
                        }

                        TextButton(
                            onClick = { viewModel.giveUp() },
                            enabled = isButtonEnabled,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        ) {
                            Text(
                                text = buttonText,
                                fontWeight = if (isButtonEnabled) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = backgroundImageRes),
                contentDescription = "Oyun Ekranı Arka Planı",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = {
                            textInput = it
                            if (errorMessage != null) {
                                viewModel.clearError()
                            }
                        },
                        label = { Text("Tahmininizi girin...") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = gameWon || gameLost,
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = {
                            if (errorMessage != null) {
                                Text(text = errorMessage!!)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.8f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.7f),
                            disabledContainerColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.Black,
                            errorContainerColor = Color.White.copy(alpha = 0.8f),
                            errorSupportingTextColor = Color(0xFFB00020)
                        )
                    )

                    if (filteredList.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .heightIn(max = 200.dp)
                                .fillMaxWidth()
                                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        ) {
                            items(filteredList) { name ->
                                Text(
                                    text = name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { textInput = name }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.makeGuess(textInput)
                        textInput = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !gameWon && !gameLost
                ) {
                    Text(if (gameWon) "KAZANDINIZ!" else "TAHMİN ET")
                }

                if (gameLost) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Doğru Cevap: $correctAnswerName",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = listState
                ) {
                    items(
                        items = viewModel.guessHistory.reversed(),
                        key = { guess -> guess.entityName + guess.hashCode() }
                    ) { guess ->
                        GuessRowItem(guessRow = guess)
                    }
                }
            }
        }
    }
}