package ru.lingua.app.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.lingua.app.ui.matching.ScoreEvent
import kotlin.math.roundToInt

/** Счётчик звёзд. При изменении число плавно «докручивается», а под ним всплывает «+10» или «−3». */
@Composable
fun StarsCounter(stars: Int, event: ScoreEvent?, modifier: Modifier = Modifier) {
    val shown by animateIntAsState(targetValue = stars, animationSpec = tween(500), label = "stars")

    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Text(
            text = "⭐ $shown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        if (event != null) {
            key(event.id) {
                val progress = remember { Animatable(0f) }
                LaunchedEffect(Unit) { progress.animateTo(1f, tween(1000)) }
                Text(
                    text = event.text,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (event.positive) Color(0xFF43A047) else MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .offset { IntOffset(0, (20.dp.toPx() + progress.value * 36.dp.toPx()).roundToInt()) }
                        .alpha(1f - progress.value),
                )
            }
        }
    }
}
