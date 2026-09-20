package ru.lingua.app.game

/** Правила начисления звёзд. Меняйте числа здесь, чтобы настроить сложность. */
object Scoring {
    const val CORRECT = 10              // за правильную пару
    const val STREAK_FROM = 3           // с какой по счёту правильной пары подряд идёт бонус
    const val STREAK_BONUS = 5          // бонус за каждую пару в серии
    const val WRONG_PENALTY = 3         // сколько отнимаем за ошибку (звёзд не бывает меньше 0)
    const val PERFECT_ROUND_BONUS = 20  // за раунд без единой ошибки
}

/** Сколько миллисекунд держать палец на аватаре, чтобы открыть раздел для родителей. */
const val PARENT_HOLD_MS = 3000L
