package com.example.mindquest.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = QuestPurple80,
    secondary = QuestTeal80,
    tertiary = QuestOrange80
)

private val LightColorScheme = lightColorScheme(
    primary = QuestPurple40,
    secondary = QuestTeal40,
    tertiary = QuestOrange40
)

@Composable
fun MindQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is intentionally off by default: a consistent, kid-friendly palette
    // reads better for young learners than a per-device wallpaper-derived theme.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
