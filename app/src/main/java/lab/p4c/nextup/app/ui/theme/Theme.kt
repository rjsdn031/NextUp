package lab.p4c.nextup.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Purple500,
    onPrimary = White,
    secondary = Teal200,
    onSecondary = Black,
    background = Black,
    onBackground = White,
    surface = Color(0xFF121212),
    onSurface = White
)

private val LightColors = lightColorScheme(
    primary = Purple500,
    onPrimary = White,
    secondary = Teal700,
    onSecondary = White,
    background = White,
    onBackground = Color(0xFF121212),
    surface = White,
    onSurface = Color(0xFF121212)
)

@Composable
fun NextUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}