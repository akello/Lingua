package ru.lingua.app.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.lingua.app.game.Progress
import kotlin.random.Random

private enum class ParentStep { Gate, Stats, ConfirmReset }

/**
 * Раздел для родителей. Три шага:
 * 1. Пример на умножение — ребёнок случайно не пройдёт.
 * 2. Статистика и кнопка сброса.
 * 3. Подтверждение сброса.
 */
@Composable
fun ParentDialog(progress: Progress, onReset: () -> Unit, onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(ParentStep.Gate) }

    when (step) {
        ParentStep.Gate -> ParentGate(onPassed = { step = ParentStep.Stats }, onDismiss = onDismiss)

        ParentStep.Stats -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Для родителей") },
            text = {
                val total = progress.totalCorrect + progress.totalWrong
                val accuracy = if (total == 0) 0 else progress.totalCorrect * 100 / total
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Звание: ${progress.rank.emoji} ${progress.rank.title}")
                    Text("Звёзд: ${progress.stars}")
                    Text("Правильных пар: ${progress.totalCorrect}")
                    Text("Ошибок: ${progress.totalWrong} (точность $accuracy%)")
                    Text("Раундов пройдено: ${progress.roundsCompleted}, без ошибок: ${progress.perfectRounds}")
                    Text("Лучшая серия подряд: ${progress.bestStreak}")
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Закрыть") }
            },
            dismissButton = {
                TextButton(
                    onClick = { step = ParentStep.ConfirmReset },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Сбросить достижения") }
            },
        )

        ParentStep.ConfirmReset -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Сбросить всё?") },
            text = { Text("Звёзды, звание и статистика обнулятся. Отменить это будет нельзя.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReset()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Сбросить") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun ParentGate(onPassed: () -> Unit, onDismiss: () -> Unit) {
    var a by remember { mutableIntStateOf(Random.nextInt(13, 30)) }
    var b by remember { mutableIntStateOf(Random.nextInt(6, 10)) }
    var answer by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Только для взрослых") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Решите пример: $a × $b = ?")
                OutlinedTextField(
                    value = answer,
                    onValueChange = { value ->
                        answer = value.filter { it.isDigit() }.take(4)
                        wrong = false
                    },
                    singleLine = true,
                    isError = wrong,
                    supportingText = { if (wrong) Text("Неверно, вот другой пример") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (answer.toIntOrNull() == a * b) {
                    onPassed()
                } else {
                    // Неверно — даём новый пример, чтобы нельзя было подобрать ответ
                    wrong = true
                    answer = ""
                    a = Random.nextInt(13, 30)
                    b = Random.nextInt(6, 10)
                }
            }) { Text("Далее") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}
