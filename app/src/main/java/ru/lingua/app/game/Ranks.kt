package ru.lingua.app.game

import androidx.compose.ui.graphics.Color

/**
 * Звание. minStars — сколько звёзд нужно, чтобы его получить.
 * backgroundTop/Bottom — цвета фона приложения на этом звании.
 */
data class Rank(
    val index: Int,
    val title: String,
    val emoji: String,
    val minStars: Int,
    val backgroundTop: Color,
    val backgroundBottom: Color,
)

/** Лестница званий: от цыплёнка до дракона. Раунд без ошибок даёт примерно 130 звёзд. */
val RANKS: List<Rank> = listOf(
    Rank(0, "Цыплёнок", "🐣", 0, Color(0xFFFFFBEA), Color(0xFFFFF0B3)),
    Rank(1, "Котёнок", "🐱", 100, Color(0xFFFFF6EC), Color(0xFFFFDDB5)),
    Rank(2, "Щенок", "🐶", 250, Color(0xFFFFF0EB), Color(0xFFFFCDB8)),
    Rank(3, "Зайчишка", "🐰", 500, Color(0xFFF5FAEE), Color(0xFFD9ECC4)),
    Rank(4, "Хитрый лис", "🦊", 800, Color(0xFFFFF3E6), Color(0xFFFFC999)),
    Rank(5, "Мудрая сова", "🦉", 1200, Color(0xFFEDF7F5), Color(0xFFB9DFD8)),
    Rank(6, "Смелый волк", "🐺", 1700, Color(0xFFEEF4FB), Color(0xFFC0D8F0)),
    Rank(7, "Сильный медведь", "🐻", 2300, Color(0xFFF6F0EA), Color(0xFFDCC5AE)),
    Rank(8, "Зоркий орёл", "🦅", 3000, Color(0xFFEFF0FA), Color(0xFFC7CCEE)),
    Rank(9, "Царь зверей", "🦁", 4000, Color(0xFFFFFBE5), Color(0xFFFFE47A)),
    Rank(10, "Дракон", "🐉", 5500, Color(0xFFFDEEF4), Color(0xFFF5B8D2)),
)

/** Какое звание положено за это количество звёзд. */
fun rankForStars(stars: Int): Rank = RANKS.last { stars >= it.minStars }

/** Следующее звание или null, если это высшее. */
fun nextRank(rank: Rank): Rank? = RANKS.getOrNull(rank.index + 1)
