package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AlarmTile(
    time: String,
    days: List<String>,
    enabled: Boolean,
    onToggle: ((Boolean) -> Unit)?
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    val timeColor = if (enabled) c.onSurface else c.onSurfaceVariant
    val daysColor = if (enabled) c.onSurfaceVariant else c.onSurfaceVariant.copy(alpha = 0.6f)

    Surface(
        color = c.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = time,
                    color = timeColor,
                    style = t.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = days.joinToString(" "),
                    color = daysColor,
                    style = t.bodySmall
                )
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = c.onPrimary,
                    checkedTrackColor = c.primary,
                    uncheckedThumbColor = c.outline,
                    uncheckedTrackColor = c.background
                )
            )
        }
    }
}
