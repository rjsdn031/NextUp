package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * 알람 볼륨 설정 행.
 *
 * @param volume 0.0 ~ 1.0
 * @param onValueChange 사용자 드래그 중 호출되는 콜백
 * @param enabled 비활성화 여부
 */
@Composable
fun AlarmVolumeRow(
    volume: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val pct = (volume.coerceIn(0f, 1f) * 100f).roundToInt()
        Text(
            text = "볼륨 ${pct}%",
            style = t.bodyMedium,
            color = if (enabled) c.onSurface else c.onSurfaceVariant
        )

        Slider(
            value = volume.coerceIn(0f, 1f),
            onValueChange = onValueChange,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = c.primary,
                activeTrackColor = c.primary,
                inactiveTrackColor = c.outline.copy(alpha = 0.4f)
            )
        )
    }
}