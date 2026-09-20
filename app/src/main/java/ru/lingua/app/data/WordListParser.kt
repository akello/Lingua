package ru.lingua.app.data

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

/** Результат разбора txt-файла. */
data class ParsedWords(
    val words: List<WordPair>,
    val skippedLines: Int,   // строки, где не нашлось и русского, и английского
)

/**
 * Разбирает txt-файл со словами. Одна строка — одна пара. Подходят любые варианты:
 *
 *     кошка, cat
 *     собака dog
 *     cat - кошка          (порядок не важен)
 *     мороженое; ice cream (фразы из нескольких слов)
 *     ключ = key, clue     (несколько переводов)
 *     # строки с решёткой и пустые строки пропускаются
 *
 * Где русский, а где английский, определяем по буквам (кириллица или латиница),
 * поэтому разделитель может быть любым: запятая, точка с запятой, табуляция,
 * тире, знак «=», «|» или просто пробел.
 */
fun parseWordList(text: String): ParsedWords {
    val words = mutableListOf<WordPair>()
    var skipped = 0

    for (rawLine in text.lines()) {
        val line = rawLine.trim()
        if (line.isEmpty() || line.startsWith("#")) continue

        val ruParts = mutableListOf<String>()
        val enParts = mutableListOf<String>()

        for (part in line.split(PART_SEPARATORS)) {
            val tokens = part.split(WHITESPACE).filter { it.isNotBlank() }
            val ru = tokens.filter { it.hasCyrillic() }
            val en = tokens.filter { it.hasLatin() && !it.hasCyrillic() }
            if (ru.isNotEmpty()) ruParts += ru.joinToString(" ")
            if (en.isNotEmpty()) enParts += en.joinToString(" ")
        }

        val ruText = ruParts.joinToString(", ").cleanEdges()
        val enText = enParts.joinToString(", ").cleanEdges()
        if (ruText.isEmpty() || enText.isEmpty()) {
            skipped++
        } else {
            words += WordPair(id = words.size, ru = ruText, en = enText)
        }
    }
    return ParsedWords(words, skipped)
}

/**
 * Превращает байты файла в текст. Обычно файлы в UTF-8,
 * но txt из старого Блокнота Windows бывают в кодировке windows-1251 — поддерживаем оба.
 */
fun decodeText(bytes: ByteArray): String {
    val utf8 = Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
    val text = try {
        utf8.decode(ByteBuffer.wrap(bytes)).toString()
    } catch (e: CharacterCodingException) {
        String(bytes, Charset.forName("windows-1251"))
    }
    return text.removePrefix("﻿") // невидимая метка BOM в начале файла
}

// Разделители между частями строки: , ; | = табуляция, а также тире (длинное — всегда, дефис — только с пробелами вокруг)
private val PART_SEPARATORS = Regex("""\s*[,;|=\t]\s*|\s+-\s+|\s*[–—]\s*""")
private val WHITESPACE = Regex("""\s+""")

private fun String.hasCyrillic() = any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.CYRILLIC }
private fun String.hasLatin() = any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.LATIN }
private fun String.cleanEdges() = trim(' ', '.', '"', '«', '»', '“', '”')
