package ru.lingua.app.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.lingua.app.game.Rank

/** Фон, который зависит от звания: свой цвет и большой полупрозрачный зверь в углу. */
@Composable
fun RankBackground(rank: Rank, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    // В тёмной теме затемняем цвета, чтобы светлый текст оставался читаемым
    val top by animateColorAsState(
        targetValue = if (dark) lerp(rank.backgroundTop, Color.Black, 0.8f) else rank.backgroundTop,
        animationSpec = tween(800),
        label = "bgTop",
    )
    val bottom by animateColorAsState(
        targetValue = if (dark) lerp(rank.backgroundBottom, Color.Black, 0.7f) else rank.backgroundBottom,
        animationSpec = tween(800),
        label = "bgBottom",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, bottom))),
    ) {
        Text(
            text = rank.emoji,
            fontSize = 180.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .alpha(0.15f),
        )
        content()
    }
}
