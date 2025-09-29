package lab.p4c.nextup.ui.widget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val bg = Color(0xFF212121) // Flutter Colors.grey[900]
    val timeColor = if (enabled) Color.White else Color(0xFF9E9E9E)
    val daysColor = if (enabled) Color(0xB3FFFFFF) /* white70 */ else Color(0xFF9E9E9E)

    Surface(
        color = bg,
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
                    fontSize = androidx.compose.material3.MaterialTheme.typography.headlineMedium.fontSize, // ~28sp
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = days.joinToString(" "),
                    color = daysColor,
                    fontSize = androidx.compose.material3.MaterialTheme.typography.bodySmall.fontSize
                )
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF9E9E9E), // Flutter activeColor: Colors.grey
                    checkedTrackColor = Color(0xFFBDBDBD)
                )
            )
        }
    }
}
