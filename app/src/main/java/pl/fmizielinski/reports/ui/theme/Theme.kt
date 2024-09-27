package pl.fmizielinski.reports.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryDark,
        secondary = SecondaryDark,
        tertiary = TertiaryDark,
        background = BackgroundDark,
        surface = BackgroundDark,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = PrimaryLight,
        secondary = SecondaryLight,
        tertiary = TertiaryLight,
        background = BackgroundLight,
        surface = BackgroundLight,
        /* Other default colors to override
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
         */
    )

@Composable
fun ReportsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
