package ru.lingua.app.ui.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lingua.app.data.WordListInfo
import ru.lingua.app.game.Progress
import ru.lingua.app.ui.common.ErrorView
import ru.lingua.app.ui.common.InfoBanner
import ru.lingua.app.ui.common.sourceMessage
import ru.lingua.app.ui.game.ProfileCard
import ru.lingua.app.ui.game.RankBackground

/** Главный экран: карточка ребёнка и выбор списка слов. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    viewModel: ListsViewModel,
    progress: Progress,
    onResetProgress: () -> Unit,
    onListClick: (WordListInfo) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RankBackground(progress.rank) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Lingua") },
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
                            ElevatedCard(
                                onClick = { onListClick(list) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = list.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
