package ru.lingua.app

import android.app.Application
import ru.lingua.app.data.OnlineWordRepository
import ru.lingua.app.data.WordRepository
import ru.lingua.app.game.ProgressRepository
import ru.lingua.app.game.SettingsRepository
import ru.lingua.app.game.Speaker

/**
 * Объект всего приложения: создаётся один раз при запуске.
 * Здесь живут общие хранилища и синтезатор речи, которыми пользуются все экраны.
 */
class LinguaApplication : Application() {
    val repository: WordRepository by lazy { OnlineWordRepository(this) }
    val progressRepository: ProgressRepository by lazy { ProgressRepository(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val speaker: Speaker by lazy { Speaker(this) }
}
