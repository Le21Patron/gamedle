package com.sefa.loldle_karakter.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sefa.loldle_karakter.data.ComparisonState
import com.sefa.loldle_karakter.data.GuessRow
import kotlinx.coroutines.delay

val CorrectColor = Color(0xFF4CAF50)
val PartialColor = Color(0xFFFFC107)
val WrongColor = Color(0xFFF44336)
val BorderColor = Color.Gray

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GuessRowItem(guessRow: GuessRow) {

    var animationsHavePlayed by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(guessRow.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "${guessRow.entityName} ikonu",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, BorderColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = guessRow.entityName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val attributeVisibilityStates = remember {
            mutableStateListOf<Boolean>().apply {
                repeat(guessRow.comparisons.size) { add(animationsHavePlayed) }
            }
        }

        LaunchedEffect(Unit) {
            if (!animationsHavePlayed) {
                guessRow.comparisons.indices.forEach { index ->
                    delay(300L)
                    attributeVisibilityStates[index] = true
                }
                animationsHavePlayed = true
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            guessRow.comparisons.forEachIndexed { index, comparison ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = attributeVisibilityStates[index],
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(durationMillis = 500, easing = EaseOutCubic)
                        ) + fadeIn(animationSpec = tween(500)),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = comparison.attributeName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (comparison.state) {
                                            ComparisonState.CORRECT -> CorrectColor
                                            ComparisonState.PARTIAL -> PartialColor
                                            ComparisonState.WRONG -> WrongColor
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comparison.value,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(2.dp),
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}