package ru.lingua.app.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.lingua.app.game.Rank
import kotlin.math.sin
import kotlin.random.Random

/** Праздничный экран «Новое звание!» с конфетти. */
@Composable
fun LevelUpDialog(rank: Rank, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(rank.backgroundTop, rank.backgroundBottom))),
            contentAlignment = Alignment.Center,
        ) {
            Confetti(Modifier.fillMaxSize())

            // Зверёк «выпрыгивает» с пружинкой
            val scale = remember { Animatable(0.2f) }
            LaunchedEffect(Unit) {
                scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp),
            ) {
                Text(
                    text = "Новое звание!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TEXT_COLOR,
                )
                Text(
                    text = rank.emoji,
                    fontSize = 130.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    },
                )
                Text(
                    text = rank.title,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = TEXT_COLOR,
                )
                Spacer(Modifier.height(32.dp))
                Button(onClick = onDismiss) {
                    Text("Ура!", fontSize = 22.sp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
                }
            }
        }
    }
}

private val TEXT_COLOR = Color(0xFF3E2723)

private val CONFETTI_COLORS = listOf(
    Color(0xFFE53935), Color(0xFFFDD835), Color(0xFF43A047),
    Color(0xFF1E88E5), Color(0xFF8E24AA), Color(0xFFFB8C00),
)

private class ConfettiPiece(
    val x: Float,          // где по горизонтали (0..1)
    val speed: Float,      // сколько экранов пролетает за секунду
    val startShift: Float, // чтобы кусочки падали не одновременно
    val sizeDp: Float,
    val color: Color,
    val spin: Float,       // градусов в секунду
    val wobble: Float,     // сдвиг покачивания
)

@Composable
private fun Confetti(modifier: Modifier) {
    val pieces = remember {
        List(90) {
            ConfettiPiece(
                x = Random.nextFloat(),
                speed = 0.15f + Random.nextFloat() * 0.3f,
                startShift = Random.nextFloat(),
                sizeDp = 6f + Random.nextFloat() * 8f,
                color = CONFETTI_COLORS.random(),
                spin = Random.nextFloat() * 720f - 360f,
                wobble = Random.nextFloat() * 6.28f,
            )
        }
    }
    // Секунды с момента появления экрана, обновляются каждый кадр
    val time by produceState(0f) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now -> value = (now - start) / 1_000_000_000f }
        }
    }

    Canvas(modifier) {
        for (p in pieces) {
            val fall = (time * p.speed + p.startShift) % 1.2f - 0.1f
            val y = fall * size.height
            val x = p.x * size.width + sin(time * 2f + p.wobble) * 16.dp.toPx()
            val w = p.sizeDp.dp.toPx()
            rotate(degrees = time * p.spin, pivot = Offset(x, y)) {
                drawRect(color = p.color, topLeft = Offset(x - w / 2, y - w / 4), size = Size(w, w / 2))
            }
        }
    }
}
