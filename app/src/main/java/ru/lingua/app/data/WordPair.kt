package ru.lingua.app.data

/** Пара слов: русское и его перевод на английский. */
data class WordPair(
    val id: Int,
    val ru: String,
    val en: String,
)
