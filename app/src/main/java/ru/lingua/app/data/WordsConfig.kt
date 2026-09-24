package ru.lingua.app.data

/**
 * Где лежат списки слов: GitHub-репозиторий и папка в нём.
 * Приложение показывает все .txt-файлы из этой папки:
 * https://github.com/akello/lingua-words/tree/main/lists
 */
const val GITHUB_OWNER = "akello"
const val GITHUB_REPO = "lingua-words"
const val GITHUB_BRANCH = "main"
const val LISTS_FOLDER = "lists"

/** Ссылка на репозиторий с кодом приложения (показываем в окне «О приложении»). */
const val APP_REPO_URL = "https://github.com/akello/Lingua"

/** Ссылка на репозиторий со списками слов. */
const val WORDS_REPO_URL = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO"
