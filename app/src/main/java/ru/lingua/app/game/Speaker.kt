package ru.lingua.app.game

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Произносит английские слова встроенным в Android синтезатором речи.
 * Ничего скачивать не нужно: голос уже есть в системе (Android Speech Services).
 */
class Speaker(context: Context) {

    /** null — ещё не знаем, true — готов говорить, false — английский голос недоступен. */
    private val _isReady = MutableStateFlow<Boolean?>(null)
    val isReady: StateFlow<Boolean?> = _isReady.asStateFlow()

    private var engine: TextToSpeech? = null
    private var engineStarted = false

    init {
        engine = TextToSpeech(context.applicationContext) { status ->
            engineStarted = status == TextToSpeech.SUCCESS
            configure()
        }
    }

    private fun configure() {
        val tts = engine ?: return // ответ пришёл слишком рано — настроим при первом слове
        if (!engineStarted) {
            _isReady.value = false
            return
        }
        val result = tts.setLanguage(Locale.US)
        val ok = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        if (ok) tts.setSpeechRate(0.9f) // чуть медленнее обычного, чтобы ребёнку было понятнее
        _isReady.value = ok
    }

    /** Произносит слово. Если переводов несколько («key, clue»), читает первый. */
    fun speak(text: String) {
        if (_isReady.value == null) configure()
        if (_isReady.value != true) return
        val word = text.substringBefore(",").trim()
        if (word.isEmpty()) return
        engine?.speak(word, TextToSpeech.QUEUE_FLUSH, null, word)
    }
}
