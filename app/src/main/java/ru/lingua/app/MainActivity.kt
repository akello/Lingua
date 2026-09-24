package ru.lingua.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.lingua.app.ui.game.LevelUpDialog
import ru.lingua.app.ui.lists.ListsScreen
import ru.lingua.app.ui.lists.ListsViewModel
import ru.lingua.app.ui.matching.MatchingScreen
import ru.lingua.app.ui.matching.ExerciseMode
import ru.lingua.app.ui.matching.MatchingViewModel
import ru.lingua.app.ui.theme.LinguaTheme

/** Точка входа: Android запускает этот класс, когда пользователь открывает приложение. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LinguaTheme {
                LinguaNavigation()
            }
        }
    }
}

/** Переходы между экранами: список списков → упражнение → назад. */
@Composable
private fun LinguaNavigation() {
    val app = LocalContext.current.applicationContext as LinguaApplication
    val repository = app.repository
    val progressRepository = app.progressRepository

    val progress by progressRepository.progress.collectAsStateWithLifecycle()
    val levelUp by progressRepository.levelUp.collectAsStateWithLifecycle()

    val listsViewModel = viewModel { ListsViewModel(repository) }
    val listsState by listsViewModel.state.collectAsStateWithLifecycle()

    // id открытого списка (имя файла). rememberSaveable — чтобы пережить поворот экрана.
    var openedListId by rememberSaveable { mutableStateOf<String?>(null) }
    var openedModeName by rememberSaveable { mutableStateOf(ExerciseMode.Matching.name) }
    val openedList = listsState.lists.find { it.id == openedListId }
    val openedMode = ExerciseMode.valueOf(openedModeName)

    if (openedList == null) {
        ListsScreen(
            viewModel = listsViewModel,
            progress = progress,
            onResetProgress = progressRepository::reset,
            onListClick = { list, mode ->
                openedModeName = mode.name
                openedListId = list.id
            },
        )
    } else {
        // У каждого списка своя ViewModel (key), поэтому прогресс в разных списках не смешивается
        val matchingViewModel = viewModel(key = "exercise:$openedModeName:${openedList.id}") {
            MatchingViewModel(
                repository,
                progressRepository,
                app.settingsRepository,
                app.speaker,
                openedList,
                openedMode,
            )
        }
        BackHandler { openedListId = null } // системная кнопка «Назад»
        MatchingScreen(
            viewModel = matchingViewModel,
            onBack = { openedListId = null },
        )
    }

    // Праздничный экран поверх всего, когда получено новое звание
    levelUp?.let { rank ->
        LevelUpDialog(rank = rank, onDismiss = progressRepository::consumeLevelUp)
    }
}
