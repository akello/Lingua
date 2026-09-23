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

/**
 * Лестница званий: от цыплёнка до единорога.
 * Раунд без ошибок даёт около 130 звёзд, но повторы одного списка за день дают меньше,
 * поэтому до вершины примерно полтора-три месяца регулярных занятий.
 */
val RANKS: List<Rank> = listOf(
    Rank(0, "Цыплёнок", "🐣", 0, Color(0xFFFFFBEA), Color(0xFFFFF0B3)),
    Rank(1, "Котёнок", "🐱", 500, Color(0xFFFFF6EC), Color(0xFFFFDDB5)),
    Rank(2, "Щенок", "🐶", 1200, Color(0xFFFFF0EB), Color(0xFFFFCDB8)),
    Rank(3, "Зайчишка", "🐰", 2200, Color(0xFFF5FAEE), Color(0xFFD9ECC4)),
    Rank(4, "Утёнок", "🦆", 3500, Color(0xFFFFFDE9), Color(0xFFF2E49B)),
    Rank(5, "Хитрый лис", "🦊", 5000, Color(0xFFFFF3E6), Color(0xFFFFC999)),
    Rank(6, "Мудрая сова", "🦉", 7000, Color(0xFFEDF7F5), Color(0xFFB9DFD8)),
    Rank(7, "Смелый волк", "🐺", 9500, Color(0xFFEEF4FB), Color(0xFFC0D8F0)),
    Rank(8, "Быстрый конь", "🐴", 12500, Color(0xFFF7F1EC), Color(0xFFDCC3A8)),
    Rank(9, "Сильный медведь", "🐻", 16000, Color(0xFFF6F0EA), Color(0xFFD3B69C)),
    Rank(10, "Зоркий орёл", "🦅", 19500, Color(0xFFEFF0FA), Color(0xFFC7CCEE)),
    Rank(11, "Умный дельфин", "🐬", 23500, Color(0xFFE9F6FD), Color(0xFFA8DCF5)),
    Rank(12, "Гепард", "🐆", 28000, Color(0xFFFFF7E3), Color(0xFFF3D383)),
    Rank(13, "Царь зверей", "🦁", 33000, Color(0xFFFFFBE5), Color(0xFFFFE47A)),
    Rank(14, "Мудрый слон", "🐘", 38500, Color(0xFFF1F2F4), Color(0xFFC3C8D0)),
    Rank(15, "Синий кит", "🐋", 44500, Color(0xFFE8F1FA), Color(0xFF9CC2E8)),
    Rank(16, "Дракон", "🐉", 51500, Color(0xFFFDEEF4), Color(0xFFF5B8D2)),
    Rank(17, "Единорог", "🦄", 60000, Color(0xFFF6EEFC), Color(0xFFD5B6F2)),
)

/** Какое звание положено за это количество звёзд. */
fun rankForStars(stars: Int): Rank = RANKS.last { stars >= it.minStars }

/** Следующее звание или null, если это высшее. */
fun nextRank(rank: Rank): Rank? = RANKS.getOrNull(rank.index + 1)
