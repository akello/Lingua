package ru.lingua.app.game

/** Правила начисления звёзд. Меняйте числа здесь, чтобы настроить сложность. */
object Scoring {
    const val CORRECT = 10              // за правильную пару
    const val STREAK_FROM = 3           // с какой по счёту правильной пары подряд идёт бонус
    const val STREAK_BONUS = 5          // бонус за каждую пару в серии
    const val WRONG_PENALTY = 3         // сколько отнимаем за ошибку (звёзд не бывает меньше 0)
    const val PERFECT_ROUND_BONUS = 20  // за раунд без единой ошибки

    /**
     * Повторы одного списка за день дают всё меньше звёзд:
     * первый раунд — полностью, второй — 70%, третий — 50%, дальше — 30%.
     * Так выгоднее менять списки и возвращаться на следующий день.
     */
    val REPEAT_MULTIPLIERS = listOf(1f, 0.7f, 0.5f, 0.3f)

    fun multiplierForRound(roundsAlreadyPlayedToday: Int): Float =
        REPEAT_MULTIPLIERS.getOrElse(roundsAlreadyPlayedToday) { REPEAT_MULTIPLIERS.last() }
}

/** Сколько миллисекунд держать палец на аватаре, чтобы открыть раздел для родителей. */
const val PARENT_HOLD_MS = 3000L
