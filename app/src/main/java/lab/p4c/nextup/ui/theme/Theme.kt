package lab.p4c.nextup.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// M3 컬러 스킴: 레거시 팔레트에 맞춰 최소 매핑
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
    // Android 12+에서 시스템 동적 색을 쓰고 싶으면 true,
    // 지금은 브랜드 컬러 유지 목적이라 기본 false
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = androidx.compose.material3.Typography(),
        shapes = androidx.compose.material3.Shapes(),
        content = content
    )
}