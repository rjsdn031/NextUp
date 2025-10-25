package lab.p4c.nextup.feature.alarm.ui.ringing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.feature.alarm.ui.components.DismissSlider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RingingScreen(
    title: String,
    body: String,
    showSnooze: Boolean,
    snoozeMinutes: Int,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val now = remember { LocalDateTime.now() }
    val timeStr = now.format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA))
    val dateStr = now.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREA))

    Surface {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(88.dp))
                Text(timeStr, style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Text(dateStr, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(body, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f))
                DismissSlider(onComplete = onDismiss)
                Spacer(Modifier.height(88.dp))
            }
            if (showSnooze) {
                Button(
                    onClick = onSnooze,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp)
                ) {
                    Text("${snoozeMinutes}분 후 다시 알림")
                }
            }
        }
    }
}