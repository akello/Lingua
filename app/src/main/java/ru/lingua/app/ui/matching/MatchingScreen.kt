package ru.lingua.app.ui.matching

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lingua.app.ui.common.ErrorView
import ru.lingua.app.ui.common.InfoBanner
import ru.lingua.app.ui.common.sourceMessage
import ru.lingua.app.ui.game.RankBackground
import ru.lingua.app.ui.game.StarsCounter
import ru.lingua.app.ui.theme.CorrectDark
import ru.lingua.app.ui.theme.CorrectLight

/** Экран упражнения. Берёт данные из ViewModel и передаёт нажатия обратно. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingScreen(viewModel: MatchingViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val speechReady by viewModel.speechReady.collectAsStateWithLifecycle()

    // Лёгкая вибрация при ошибке
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(state.wrongLeft) {
        if (state.wrongLeft != null) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    RankBackground(progress.rank) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(viewModel.list.title) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    actions = {
                        // Включить или выключить озвучку
                        IconButton(onClick = viewModel::toggleSound) {
                            Text(if (soundEnabled) "🔊" else "🔇", fontSize = 20.sp)
                        }
                        // Заново скачать слова (например, после правки файла на GitHub)
                        IconButton(onClick = viewModel::load, enabled = !state.isLoading) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Обновить")
                        }
                    },
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.isLoading -> CircularProgressIndicator()
                    state.error != null -> ErrorView(state.error!!, onRetry = viewModel::load)
                    else -> MatchingBoard(
                        state = state,
                        stars = progress.stars,
                        showSpeechWarning = soundEnabled && speechReady == false,
                        onLeftClick = viewModel::onLeftClick,
                        onRightClick = viewModel::onRightClick,
                        onNextRound = viewModel::nextRound,
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchingBoard(
    state: MatchingUiState,
    stars: Int,
    showSpeechWarning: Boolean,
    onLeftClick: (Int) -> Unit,
    onRightClick: (Int) -> Unit,
    onNextRound: () -> Unit,
) {
    val total = state.leftCards.size
    val progress by animateFloatAsState(
        targetValue = if (total == 0) 0f else state.matched.size.toFloat() / total,
        label = "progress",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Раунд ${state.round} · ${state.matched.size} из $total",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (state.streak >= 2) {
                    Text(
                        text = "🔥 Серия: ${state.streak}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            StarsCounter(stars = stars, event = state.scoreEvent)
        }
        sourceMessage(state.source, what = "слова")?.let { InfoBanner(it) }
        if (showSpeechWarning) {
            InfoBanner("Английский голос не найден. Включите его в настройках Android: Специальные возможности → Синтез речи.")
        }
        if (state.skippedLines > 0) {
            InfoBanner("Не удалось разобрать строк в файле: ${state.skippedLines}")
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))

        // Два столбика: слева русские слова, справа английские
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WordColumn(
                cards = state.leftCards,
                selected = state.selectedLeft,
                wrong = state.wrongLeft,
                matched = state.matched,
                onClick = onLeftClick,
                modifier = Modifier.weight(1f),
            )
            WordColumn(
                cards = state.rightCards,
                selected = state.selectedRight,
                wrong = state.wrongRight,
                matched = state.matched,
                onClick = onRightClick,
                modifier = Modifier.weight(1f),
            )
        }

        if (state.isRoundComplete) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = if (state.mistakes == 0) "Идеально! 🎉" else "Раунд пройден! 👍 Ошибок: ${state.mistakes}",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.roundBonus > 0) {
                Text(
                    text = "Бонус за раунд без ошибок: +${state.roundBonus} ⭐",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onNextRound, modifier = Modifier.fillMaxWidth()) {
                Text("Следующий раунд", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun WordColumn(
    cards: List<WordCard>,
    selected: Int?,
    wrong: Int?,
    matched: Set<Int>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cards.forEach { card ->
            val status = when (card.pairId) {
                wrong -> CardStatus.Wrong
                in matched -> CardStatus.Matched
                selected -> CardStatus.Selected
                else -> CardStatus.Normal
            }
            WordCardView(text = card.text, status = status, onClick = { onClick(card.pairId) })
        }
    }
}

private enum class CardStatus { Normal, Selected, Matched, Wrong }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordCardView(text: String, status: CardStatus, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val correct = if (isSystemInDarkTheme()) CorrectDark else CorrectLight

    val background by animateColorAsState(
        targetValue = when (status) {
            CardStatus.Normal -> colors.surface
            CardStatus.Selected -> colors.primaryContainer
            CardStatus.Matched -> correct
            CardStatus.Wrong -> colors.errorContainer
        },
        label = "cardColor",
    )
    val alpha by animateFloatAsState(
        targetValue = if (status == CardStatus.Matched) 0.6f else 1f,
        label = "cardAlpha",
    )
    val borderColor = when (status) {
        CardStatus.Selected -> colors.primary
        CardStatus.Wrong -> colors.error
        else -> colors.outlineVariant
    }

    // Карточка «трясётся», если пара неверная
    val shake = remember { Animatable(0f) }
    LaunchedEffect(status) {
        if (status == CardStatus.Wrong) {
            repeat(3) {
                shake.animateTo(8f, tween(45))
                shake.animateTo(-8f, tween(45))
            }
            shake.animateTo(0f, tween(45))
        }
    }

    Surface(
        onClick = onClick, // по угаданной паре можно нажать ещё раз и услышать слово
        shape = RoundedCornerShape(12.dp),
        color = background,
        border = BorderStroke(if (status == CardStatus.Selected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (status == CardStatus.Matched) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .offset { IntOffset(shake.value.dp.roundToPx(), 0) }
            .alpha(alpha),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = if (status == CardStatus.Selected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
        }
    }
}
