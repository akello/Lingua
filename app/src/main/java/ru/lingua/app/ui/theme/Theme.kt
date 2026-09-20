package ru.lingua.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Цвет «правильно» — в Material нет готового зелёного, задаём сами. */
val CorrectLight = Color(0xFFC8E6C9)
val CorrectDark = Color(0xFF1B5E20)

private val LightColors = lightColorScheme(primary = Color(0xFF3F51B5))
private val DarkColors = darkColorScheme(primary = Color(0xFF9FA8DA))

@Composable
fun LinguaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = when {
        // На Android 12+ берём цвета из обоев телефона
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
