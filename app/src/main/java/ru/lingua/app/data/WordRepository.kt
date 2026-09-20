package ru.lingua.app.data

/** Откуда взяты данные — чтобы показать это пользователю. */
enum class WordSource {
    Online,   // только что скачали из интернета
    Cache,    // интернета нет, взяли сохранённую копию
    Builtin,  // интернета нет и копии нет — встроенные в приложение
}

/**
 * Список слов (один txt-файл).
 * id — имя файла, title — название для экрана, url — откуда скачивать (null у встроенных).
 */
data class WordListInfo(
    val id: String,
    val title: String,
    val url: String?,
)

data class LoadedLists(
    val lists: List<WordListInfo>,
    val source: WordSource,
)

data class LoadedWords(
    val words: List<WordPair>,
    val source: WordSource,
    val skippedLines: Int = 0,
)

/**
 * Откуда приложение берёт списки и слова.
 * Экраны знают только про этот интерфейс и не зависят от того, где лежат файлы.
 */
interface WordRepository {
    suspend fun loadLists(): LoadedLists
    suspend fun loadWords(list: WordListInfo): LoadedWords
}

/** «01_Животные.txt» → «Животные». Цифры в начале имени нужны только для порядка. */
fun titleFromFileName(fileName: String): String =
    fileName.removeSuffix(".txt")
        .replace('_', ' ')
        .replaceFirst(Regex("""^\d+[\s.\-]*"""), "")
        .trim()
        .ifEmpty { fileName }
