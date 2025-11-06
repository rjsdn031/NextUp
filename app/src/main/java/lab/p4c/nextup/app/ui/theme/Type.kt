package lab.p4c.nextup.app.ui.theme

import androidx.compose.material3.Typography as M3Typography
import androidx.compose.material3.Shapes as M3Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// 필요 시 폰트 커스터마이징
val NextUpTypography = M3Typography()

val NextUpShapes = M3Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
