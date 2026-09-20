package ru.lingua.app.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Загружает списки слов из папки на GitHub.
 *
 * Для всего действует один порядок:
 * 1. Скачиваем из интернета. Получилось — сохраняем копию на телефоне.
 * 2. Не получилось — берём копию, сохранённую в прошлый раз.
 * 3. Копии нет — берём встроенные файлы из app/src/main/assets/lists.
 */
class OnlineWordRepository(private val context: Context) : WordRepository {

    private val cacheDir = File(context.filesDir, "lists").apply { mkdirs() }
    private val indexFile = File(context.filesDir, "lists_index.json")

    // ---------- Какие списки есть ----------

    override suspend fun loadLists(): LoadedLists = withContext(Dispatchers.IO) {
        // 1. Спрашиваем у GitHub, какие файлы лежат в папке
        try {
            val lists = fetchListsFromGitHub()
            if (lists.isEmpty()) throw IOException("В папке $LISTS_FOLDER нет .txt-файлов")
            indexFile.writeText(listsToJson(lists))
            return@withContext LoadedLists(lists, WordSource.Online)
        } catch (e: Exception) {
            Log.w(TAG, "Не удалось получить списки: ${e.message}")
        }

        // 2. Сохранённый в прошлый раз перечень
        try {
            if (indexFile.exists()) {
                val lists = listsFromJson(indexFile.readText())
                if (lists.isNotEmpty()) return@withContext LoadedLists(lists, WordSource.Cache)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Сохранённый перечень повреждён: ${e.message}")
        }

        // 3. Встроенные списки
        LoadedLists(builtinLists(), WordSource.Builtin)
    }

    private fun fetchListsFromGitHub(): List<WordListInfo> {
        val api = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/contents/$LISTS_FOLDER?ref=$GITHUB_BRANCH"
        val array = JSONArray(decodeText(downloadBytes(api, accept = "application/vnd.github+json")))
        val lists = mutableListOf<WordListInfo>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val name = item.getString("name")
            if (item.optString("type") == "file" && name.endsWith(".txt", ignoreCase = true)) {
                lists += WordListInfo(
                    id = name,
                    title = titleFromFileName(name),
                    url = item.getString("download_url"),
                )
            }
        }
        return lists.sortedBy { it.id.lowercase() }
    }

    private fun builtinLists(): List<WordListInfo> =
        (context.assets.list(ASSETS_FOLDER) ?: emptyArray())
            .filter { it.endsWith(".txt", ignoreCase = true) }
            .sortedBy { it.lowercase() }
            .map { WordListInfo(id = it, title = titleFromFileName(it), url = null) }

    // ---------- Слова одного списка ----------

    override suspend fun loadWords(list: WordListInfo): LoadedWords = withContext(Dispatchers.IO) {
        val cacheFile = File(cacheDir, list.id)

        // 1. Интернет
        if (list.url != null) {
            try {
                val bytes = downloadBytes(list.url)
                val parsed = parseWordList(decodeText(bytes))
                if (parsed.words.size < 2) throw IOException("В файле слишком мало слов: ${parsed.words.size}")
                cacheFile.writeBytes(bytes)
                return@withContext LoadedWords(parsed.words, WordSource.Online, parsed.skippedLines)
            } catch (e: Exception) {
                Log.w(TAG, "Не удалось скачать ${list.id}: ${e.message}")
            }
        }

        // 2. Сохранённая копия
        if (cacheFile.exists()) {
            val parsed = parseWordList(decodeText(cacheFile.readBytes()))
            if (parsed.words.size >= 2) {
                return@withContext LoadedWords(parsed.words, WordSource.Cache, parsed.skippedLines)
            }
        }

        // 3. Встроенный файл с таким же именем
        val builtin = builtinLists().any { it.id == list.id }
        if (builtin) {
            val bytes = context.assets.open("$ASSETS_FOLDER/${list.id}").use { it.readBytes() }
            val parsed = parseWordList(decodeText(bytes))
            return@withContext LoadedWords(parsed.words, WordSource.Builtin, parsed.skippedLines)
        }

        throw IOException("Нет интернета, а этот список ещё ни разу не скачивался")
    }

    // ---------- Сеть ----------

    private fun downloadBytes(address: String, accept: String? = null): ByteArray {
        val connection = URL(address).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.useCaches = false
        connection.setRequestProperty("User-Agent", "Lingua-Android")
        if (accept != null) connection.setRequestProperty("Accept", accept)
        try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw IOException("Сервер ответил кодом $code")
            return connection.inputStream.use { it.readBytes() }
        } finally {
            connection.disconnect()
        }
    }

    // ---------- Сохранение перечня списков ----------

    private fun listsToJson(lists: List<WordListInfo>): String {
        val array = JSONArray()
        lists.forEach {
            array.put(JSONObject().put("id", it.id).put("title", it.title).put("url", it.url))
        }
        return array.toString()
    }

    private fun listsFromJson(json: String): List<WordListInfo> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            WordListInfo(
                id = item.getString("id"),
                title = item.getString("title"),
                url = item.optString("url").ifEmpty { null },
            )
        }
    }

    private companion object {
        const val TAG = "OnlineWordRepository"
        const val ASSETS_FOLDER = "lists"
    }
}
