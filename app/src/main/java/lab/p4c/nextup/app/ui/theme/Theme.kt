package lab.p4c.nextup.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.graphics.toArgb

private val NextUpDarkScheme = darkColorScheme(
    primary            = Tangerine,
    onPrimary          = TextPrimary,
    primaryContainer   = TangerineStrong,
    onPrimaryContainer = Color.White,

    secondary            = TealSecondary,
    onSecondary          = TextPrimary,
    secondaryContainer   = TealSecondaryMuted,
    onSecondaryContainer = Color.Black,

    tertiary          = AquaAccent,
    onTertiary        = Color.Black,

    background        = BgCharcoal,
    onBackground      = TextPrimary,

    surface           = SurfaceOnyx,
    onSurface         = TextPrimary,
    surfaceVariant    = DividerSlate,
    onSurfaceVariant  = TextSecondary,

    error             = ErrorRed,
    onError           = Color.White,

    outline           = DividerSlate,
    inverseSurface    = Color(0xFF0F0F0F),
    inverseOnSurface  = TextPrimary,
    scrim             = Color.Black
)

private val LocalExtraColors = staticCompositionLocalOf {
    ExtraColors(
        primaryMuted   = MelonMuted,
        secondaryMuted = TealSecondaryMuted,
        accent         = AquaAccent,
        success        = SuccessGreen,
        warning        = WarningAmber,
        divider        = DividerSlate,
        surfaceHover   = SurfaceHover,
        focusRing      = TangerineLight,
        hoverOverlay   = Tangerine.copy(alpha = HoverAlpha),
        pressedOverlay = Tangerine.copy(alpha = PressedAlpha),
        textSecondary  = TextSecondary,
        textMuted      = TextMuted,
        disabled       = DisabledGray
    )
}

object NextUpThemeTokens {
    val colors: ExtraColors
        @Composable @ReadOnlyComposable
        get() = LocalExtraColors.current
}

@Composable
fun NextUpTheme(
    content: @Composable () -> Unit
) {
    val scheme = NextUpDarkScheme

    ApplySystemBars(scheme)

    CompositionLocalProvider(
        LocalExtraColors provides LocalExtraColors.current.copy(
            hoverOverlay   = scheme.primary.copy(alpha = HoverAlpha),
            pressedOverlay = scheme.primary.copy(alpha = PressedAlpha)
        )
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography  = NextUpTypography,
            shapes      = NextUpShapes
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.background)
            ) {

                content()

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(scheme.background)
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .fillMaxSize(fraction = 1f)
                )

                // 4) 네비게이션 바 영역 뒤에 별도 색(보통 surface) 덮어 그리기
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(scheme.surface)
                        .windowInsetsBottomHeight(WindowInsets.navigationBars)
                        .fillMaxSize(fraction = 1f)
                )
            }
        }
    }
}

@Composable
private fun ApplySystemBars(scheme: androidx.compose.material3.ColorScheme) {
    val view = LocalView.current
    val activity = view.context.findActivity() ?: return
    val window = activity.window

    SideEffect {
        WindowCompat.setDecorFitsSystemWindows(window, true)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        if (android.os.Build.VERSION.SDK_INT < 35) {
            val statusColor = scheme.background.toArgb()
            val navColor = scheme.surface.toArgb()
            window.setStatusBarColor(statusColor)
            window.setNavigationBarColor(navColor)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                window.setNavigationBarDividerColor(Color.Transparent.toArgb())
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }
}



/** Context에서 안전하게 Activity 찾기 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}