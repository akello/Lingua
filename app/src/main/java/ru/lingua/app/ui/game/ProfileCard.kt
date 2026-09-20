package ru.lingua.app.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.lingua.app.game.PARENT_HOLD_MS
import ru.lingua.app.game.Progress
import ru.lingua.app.game.RANKS
import ru.lingua.app.game.nextRank

/**
 * Карточка ребёнка: зверёк-звание, звёзды и сколько осталось до следующего звания.
 * Короткое нажатие на зверька — лестница званий.
 * Удержание 3 секунды — скрытый раздел для родителей.
 */
@Composable
fun ProfileCard(progress: Progress, onReset: () -> Unit, modifier: Modifier = Modifier) {
    var showRanks by remember { mutableStateOf(false) }
    var showParent by remember { mutableStateOf(false) }

    val rank = progress.rank
    val next = nextRank(rank)

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(rank.backgroundBottom)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            // true — отпустили быстро, false — жест отменён, null — держали дольше PARENT_HOLD_MS
                            val released = withTimeoutOrNull(PARENT_HOLD_MS) { waitForUpOrCancellation() != null }
                            when (released) {
                                null -> showParent = true
                                true -> showRanks = true
                                false -> Unit
                            }
                        }
                    },
            ) {
                Text(rank.emoji, fontSize = 44.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(rank.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("⭐ ${progress.stars}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (next != null) {
                    val fraction = (progress.stars - rank.minStars).toFloat() / (next.minStars - rank.minStars)
                    LinearProgressIndicator(
                        progress = { fraction.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "До «${next.title}» ${next.emoji}: ещё ${maxOf(0, next.minStars - progress.stars)} ⭐",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text("Высшее звание! 🏆", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    if (showRanks) {
        RanksDialog(progress = progress, onDismiss = { showRanks = false })
    }
    if (showParent) {
        ParentDialog(progress = progress, onReset = onReset, onDismiss = { showParent = false })
    }
}

/** Все звания: полученные видны, будущие спрятаны за знаком вопроса. */
@Composable
private fun RanksDialog(progress: Progress, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Звания") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                RANKS.forEach { r ->
                    val opened = r.index <= progress.rankIndex
                    val current = r.index == progress.rankIndex
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (current) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(if (opened) r.emoji else "❓", fontSize = 28.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = if (opened) r.title else "???",
                                fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                            )
                            Text("${r.minStars} ⭐", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        },
    )
}
