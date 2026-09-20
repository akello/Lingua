package ru.lingua.app.game

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Достижения ребёнка. */
data class Progress(
    val stars: Int = 0,
    val rankIndex: Int = 0,        // звание не понижается, даже если звёзд стало меньше
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val roundsCompleted: Int = 0,
    val perfectRounds: Int = 0,
    val bestStreak: Int = 0,
) {
    val rank: Rank get() = RANKS[rankIndex.coerceIn(0, RANKS.lastIndex)]
}

/**
 * Хранит достижения на телефоне (SharedPreferences — простое хранилище «ключ → значение»).
 * Все методы вызываются из ViewModel на главном потоке.
 */
class ProgressRepository(context: Context) {

    private val prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    private val _progress = MutableStateFlow(read())
    val progress: StateFlow<Progress> = _progress.asStateFlow()

    /** Новое звание, которое ещё не показали ребёнку (для праздничного экрана). */
    private val _levelUp = MutableStateFlow<Rank?>(null)
    val levelUp: StateFlow<Rank?> = _levelUp.asStateFlow()

    /** Правильная пара. streak — сколько правильных подряд, включая эту. Возвращает, сколько звёзд добавили. */
    fun addCorrect(streak: Int): Int {
        val delta = Scoring.CORRECT + if (streak >= Scoring.STREAK_FROM) Scoring.STREAK_BONUS else 0
        update {
            it.copy(
                stars = it.stars + delta,
                totalCorrect = it.totalCorrect + 1,
                bestStreak = maxOf(it.bestStreak, streak),
            )
        }
        return delta
    }

    /** Ошибка. Возвращает, сколько звёзд реально отняли (0, если отнимать было нечего). */
    fun addWrong(): Int {
        val before = _progress.value.stars
        val after = maxOf(0, before - Scoring.WRONG_PENALTY)
        update { it.copy(stars = after, totalWrong = it.totalWrong + 1) }
        return before - after
    }

    /** Раунд пройден. Возвращает бонус (0, если были ошибки). */
    fun completeRound(perfect: Boolean): Int {
        val bonus = if (perfect) Scoring.PERFECT_ROUND_BONUS else 0
        update {
            it.copy(
                stars = it.stars + bonus,
                roundsCompleted = it.roundsCompleted + 1,
                perfectRounds = it.perfectRounds + if (perfect) 1 else 0,
            )
        }
        return bonus
    }

    fun consumeLevelUp() {
        _levelUp.value = null
    }

    fun reset() {
        prefs.edit().clear().apply()
        _progress.value = Progress()
        _levelUp.value = null
    }

    private fun update(change: (Progress) -> Progress) {
        var new = change(_progress.value)
        val earned = rankForStars(new.stars).index
        if (earned > new.rankIndex) {
            new = new.copy(rankIndex = earned)
            _levelUp.value = RANKS[earned]
        }
        _progress.value = new
        save(new)
    }

    private fun read() = Progress(
        stars = prefs.getInt(KEY_STARS, 0),
        rankIndex = prefs.getInt(KEY_RANK, 0),
        totalCorrect = prefs.getInt(KEY_CORRECT, 0),
        totalWrong = prefs.getInt(KEY_WRONG, 0),
        roundsCompleted = prefs.getInt(KEY_ROUNDS, 0),
        perfectRounds = prefs.getInt(KEY_PERFECT, 0),
        bestStreak = prefs.getInt(KEY_STREAK, 0),
    )

    private fun save(p: Progress) {
        prefs.edit()
            .putInt(KEY_STARS, p.stars)
            .putInt(KEY_RANK, p.rankIndex)
            .putInt(KEY_CORRECT, p.totalCorrect)
            .putInt(KEY_WRONG, p.totalWrong)
            .putInt(KEY_ROUNDS, p.roundsCompleted)
            .putInt(KEY_PERFECT, p.perfectRounds)
            .putInt(KEY_STREAK, p.bestStreak)
            .apply()
    }

    private companion object {
        const val KEY_STARS = "stars"
        const val KEY_RANK = "rank"
        const val KEY_CORRECT = "total_correct"
        const val KEY_WRONG = "total_wrong"
        const val KEY_ROUNDS = "rounds"
        const val KEY_PERFECT = "perfect_rounds"
        const val KEY_STREAK = "best_streak"
    }
}
