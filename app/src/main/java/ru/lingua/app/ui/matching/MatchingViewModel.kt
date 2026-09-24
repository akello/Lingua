package ru.lingua.app.ui.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.lingua.app.data.WordListInfo
import ru.lingua.app.data.WordPair
import ru.lingua.app.data.WordRepository
import ru.lingua.app.data.WordSource
import ru.lingua.app.game.Progress
import ru.lingua.app.game.ProgressRepository
import ru.lingua.app.game.SettingsRepository
import ru.lingua.app.game.Speaker

/** Сколько пар слов в одном раунде. */
const val PAIRS_PER_ROUND = 8

/** Карточка со словом. pairId одинаковый у русского слова и его перевода. */
data class WordCard(val pairId: Int, val text: String)

/** Всплывающая надпись «+10» / «−3». id нужен, чтобы анимация запускалась заново на каждое событие. */
data class ScoreEvent(val id: Long, val text: String, val positive: Boolean)

/** Всё, что нужно экрану, чтобы нарисовать себя. */
data class MatchingUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val source: WordSource? = null,               // откуда взялись слова
    val skippedLines: Int = 0,                     // строки файла, которые не удалось разобрать
    val round: Int = 0,
    val leftCards: List<WordCard> = emptyList(),   // русские слова
    val rightCards: List<WordCard> = emptyList(),  // английские слова
    val selectedLeft: Int? = null,                 // pairId выбранной карточки слева
    val selectedRight: Int? = null,                // pairId выбранной карточки справа
    val matched: Set<Int> = emptySet(),            // уже угаданные пары
    val wrongLeft: Int? = null,                    // неверная попытка (подсвечиваем красным)
    val wrongRight: Int? = null,
    val mistakes: Int = 0,                         // ошибок в этом раунде
    val streak: Int = 0,                           // правильных ответов подряд
    val roundBonus: Int = 0,                       // бонус за раунд без ошибок
    val starMultiplier: Float = 1f,                // множитель звёзд: повторы списка за день дают меньше
    val scoreEvent: ScoreEvent? = null,
) {
    val isRoundComplete: Boolean
        get() = leftCards.isNotEmpty() && matched.size == leftCards.size
}

/**
 * Логика упражнения «Соедини слова».
 * ViewModel переживает поворот экрана, поэтому прогресс не теряется.
 */
class MatchingViewModel(
    private val repository: WordRepository,
    private val progressRepository: ProgressRepository,
    private val settingsRepository: SettingsRepository,
    private val speaker: Speaker,
    val list: WordListInfo,                        // какой список слов тренируем
    val mode: ExerciseMode,                        // как тренируем
) : ViewModel() {

    private val _state = MutableStateFlow(MatchingUiState())
    val state: StateFlow<MatchingUiState> = _state.asStateFlow()

    /** Звёзды и звание — для счётчика и фона. */
    val progress: StateFlow<Progress> = progressRepository.progress

    /** Включена ли озвучка и готов ли синтезатор речи. */
    val soundEnabled: StateFlow<Boolean> = settingsRepository.soundEnabled
    val speechReady: StateFlow<Boolean?> = speaker.isReady

    fun toggleSound() = settingsRepository.setSoundEnabled(!soundEnabled.value)

    private var allWords: List<WordPair> = emptyList()
    private var queue: ArrayDeque<WordPair> = ArrayDeque()
    private var eventCounter = 0L

    init {
        load()
    }

    fun load() {
        _state.value = MatchingUiState(isLoading = true)
        queue = ArrayDeque()
        viewModelScope.launch {
            try {
                val loaded = repository.loadWords(list)
                allWords = loaded.words
                if (allWords.size < 2) {
                    _state.value = MatchingUiState(isLoading = false, error = "В списке слишком мало слов")
                } else {
                    _state.update { it.copy(source = loaded.source, skippedLines = loaded.skippedLines) }
                    nextRound()
                }
            } catch (e: Exception) {
                _state.value = MatchingUiState(isLoading = false, error = "Не удалось загрузить слова: ${e.message}")
            }
        }
    }

    /** Берём следующие 8 слов. Когда слова закончились — перемешиваем список заново. */
    fun nextRound() {
        if (queue.size < PAIRS_PER_ROUND) {
            val remaining = queue.toList()
            queue = ArrayDeque(remaining + allWords.shuffled().filter { it !in remaining })
        }
        // Следим, чтобы в одном раунде не было двух одинаковых слов — иначе их не различить
        val words = mutableListOf<WordPair>()
        val postponed = mutableListOf<WordPair>()
        while (words.size < PAIRS_PER_ROUND && queue.isNotEmpty()) {
            val w = queue.removeFirst()
            val duplicate = words.any { it.ru.equals(w.ru, ignoreCase = true) || it.en.equals(w.en, ignoreCase = true) }
            if (duplicate) postponed += w else words += w
        }
        postponed.asReversed().forEach { queue.addFirst(it) }

        // Повторный раунд по этому же списку за сегодня даёт меньше звёзд
        val multiplier = progressRepository.startRound("${list.id}#${mode.name}")

        _state.update {
            MatchingUiState(
                isLoading = false,
                round = it.round + 1,
                starMultiplier = multiplier,
                source = it.source,
                skippedLines = it.skippedLines,
                streak = it.streak,                // серия продолжается в следующем раунде
                // оба столбика перемешиваем независимо
                leftCards = words.map { w -> WordCard(w.id, w.ru) }.shuffled(),
                rightCards = words.map { w -> WordCard(w.id, w.en) }.shuffled(),
            )
        }
    }

    fun onLeftClick(pairId: Int) = onCardClick(pairId, isLeft = true)

    fun onRightClick(pairId: Int) = onCardClick(pairId, isLeft = false)

    private fun onCardClick(pairId: Int, isLeft: Boolean) {
        val s = _state.value
        // Пока мигает ошибка, нажатия игнорируем
        if (s.wrongLeft != null) return
        // По угаданной паре можно нажать ещё раз, чтобы услышать слово снова
        if (pairId in s.matched) {
            say(s, pairId, force = mode == ExerciseMode.Listening)
            return
        }
        // В режиме «Слушай и выбирай» нажатие на скрытую карточку произносит слово
        if (!isLeft && mode == ExerciseMode.Listening) {
            say(s, pairId, force = true)
        }

        val newState = if (isLeft) {
            s.copy(selectedLeft = if (s.selectedLeft == pairId) null else pairId)
        } else {
            s.copy(selectedRight = if (s.selectedRight == pairId) null else pairId)
        }

        val left = newState.selectedLeft
        val right = newState.selectedRight
        if (left == null || right == null) {
            _state.value = newState
            return
        }

        if (left == right) onCorrect(newState, left) else onWrong(newState, left, right)
    }

    private fun onCorrect(s: MatchingUiState, pairId: Int) {
        say(s, pairId)
        val streak = s.streak + 1
        val delta = progressRepository.addCorrect(streak, s.starMultiplier)
        val text = if (streak >= 3) "+$delta 🔥$streak" else "+$delta"

        var newState = s.copy(
            matched = s.matched + pairId,
            selectedLeft = null,
            selectedRight = null,
            streak = streak,
            scoreEvent = ScoreEvent(++eventCounter, text, positive = true),
        )
        if (newState.isRoundComplete) {
            val bonus = progressRepository.completeRound(perfect = newState.mistakes == 0, multiplier = s.starMultiplier)
            newState = newState.copy(roundBonus = bonus)
        }
        _state.value = newState
    }

    /** Произносит английское слово этой пары, если озвучка включена. */
    private fun say(s: MatchingUiState, pairId: Int, force: Boolean = false) {
        if (!force && !soundEnabled.value) return
        val word = s.rightCards.find { it.pairId == pairId }?.text ?: return
        speaker.speak(word)
    }

    private fun onWrong(s: MatchingUiState, left: Int, right: Int) {
        val lost = progressRepository.addWrong()
        // Подсвечиваем обе карточки красным и через мгновение сбрасываем
        _state.value = s.copy(
            wrongLeft = left,
            wrongRight = right,
            mistakes = s.mistakes + 1,
            streak = 0,
            scoreEvent = if (lost > 0) ScoreEvent(++eventCounter, "−$lost", positive = false) else s.scoreEvent,
        )
        viewModelScope.launch {
            delay(700)
            _state.update {
                it.copy(wrongLeft = null, wrongRight = null, selectedLeft = null, selectedRight = null)
            }
        }
    }
}
