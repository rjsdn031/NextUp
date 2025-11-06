package lab.p4c.nextup.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Brand palette
val Tangerine          = Color(0xFFF97316) // Primary
val TangerineLight     = Color(0xFFFB923C) // Focus/hover ring 등
val TangerineStrong    = Color(0xFFEA580C) // Active button 등
val MelonMuted         = Color(0xFFFDBA74) // Primary Muted (soft highlight)

val TealSecondary      = Color(0xFF3AA6A6) // Secondary (Muted Teal)
val TealSecondaryMuted = Color(0xFF67C3B8) // Secondary Muted
val AquaAccent         = Color(0xFF7FE3CC) // Accent (Soft Aquamarine)

val SuccessGreen       = Color(0xFF22C55E)
val WarningAmber       = Color(0xFFFBBF24)
val ErrorRed           = Color(0xFFEF4444)

// Surfaces
val BgCharcoal         = Color(0xFF1F1F1F) // Background
val SurfaceOnyx        = Color(0xFF272727) // Surface
val SurfaceHover       = Color(0xFF2E2E2E) // Surface hover
val DividerSlate       = Color(0xFF3F3F46)

// Texts
val TextPrimary        = Color(0xFFF4F4F5)
val TextSecondary      = Color(0xFFA1A1AA)
val TextMuted          = Color(0xFF71717A)
val DisabledGray       = Color(0xFF3A3A3A)

// Overlays
const val HoverAlpha    = 0.12f
const val PressedAlpha  = 0.24f

@Immutable
data class ExtraColors(
    val primaryMuted: Color,
    val secondaryMuted: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val divider: Color,
    val surfaceHover: Color,
    val focusRing: Color,
    val hoverOverlay: Color,
    val pressedOverlay: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val disabled: Color
)
