package ru.lingua.app.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.lingua.app.data.WordSource

/** Небольшая плашка с пояснением. */
@Composable
fun InfoBanner(text: String) {
    Spacer(Modifier.height(8.dp))
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

/** Текст плашки, если данные взяты не из интернета. null — если всё скачалось. */
fun sourceMessage(source: WordSource?, what: String): String? = when (source) {
    WordSource.Cache -> "Нет связи с сервером — показаны $what, скачанные ранее"
    WordSource.Builtin -> "Нет связи с сервером — показаны встроенные $what"
    else -> null
}
