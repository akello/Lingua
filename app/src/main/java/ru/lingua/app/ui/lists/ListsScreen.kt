package ru.lingua.app.ui.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lingua.app.data.WordListInfo
import ru.lingua.app.game.Progress
import ru.lingua.app.ui.common.AboutDialog
import ru.lingua.app.ui.common.ErrorView
import ru.lingua.app.ui.common.InfoBanner
import ru.lingua.app.ui.common.sourceMessage
import ru.lingua.app.ui.game.ProfileCard
import ru.lingua.app.ui.game.RankBackground
import ru.lingua.app.ui.matching.ExerciseMode

/** Главный экран: карточка ребёнка и выбор списка слов. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    viewModel: ListsViewModel,
    progress: Progress,
    onResetProgress: () -> Unit,
    onListClick: (WordListInfo, ExerciseMode) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAbout by remember { mutableStateOf(false) }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    RankBackground(progress.rank) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        // Нажатие на название — окно с версией и ссылками
                        Text("Lingua", modifier = Modifier.clickable { showAbout = true })
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    actions = {
                        IconButton(onClick = viewModel::load, enabled = !state.isLoading) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Обновить списки")
                        }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    ProfileCard(progress = progress, onReset = onResetProgress)
                }
                item {
                    Text(
                        text = "Выбери список слов",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                when {
                    state.isLoading -> item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null -> item {
                        ErrorView(state.error!!, onRetry = viewModel::load)
                    }
                    else -> {
                        sourceMessage(state.source, what = "списки")?.let { message ->
                            item { InfoBanner(message) }
                        }
                        items(state.lists, key = { it.id }) { list ->
                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = list.title,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    // Один список — два способа потренироваться
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ExerciseMode.entries.forEach { mode ->
                                            FilledTonalButton(
                                                onClick = { onListClick(list, mode) },
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text("${mode.emoji} ${mode.shortTitle}", maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
