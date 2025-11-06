package lab.p4c.nextup.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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

    CompositionLocalProvider(
        LocalExtraColors provides LocalExtraColors.current.copy(
            hoverOverlay   = scheme.primary.copy(alpha = HoverAlpha),
            pressedOverlay = scheme.primary.copy(alpha = PressedAlpha)
        )
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography  = NextUpTypography,
            shapes      = NextUpShapes,
            content     = content
        )
    }
}
