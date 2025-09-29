package lab.p4c.nextup.ui.screen.ringing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RingingScreen(
    title: String,
    body: String,
    showSnooze: Boolean,
    snoozeMinutes: Int,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val now = remember { java.time.LocalDateTime.now() }
    val timeStr = now.format(java.time.format.DateTimeFormatter.ofPattern("a h:mm", java.util.Locale.KOREA))
    val dateStr = now.format(java.time.format.DateTimeFormatter.ofPattern("M월 d일 EEEE", java.util.Locale.KOREA))

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

@Composable
private fun DismissSlider(onComplete: () -> Unit) {
    var v by remember { mutableFloatStateOf(0f) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("밀어서 해제", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Slider(
            value = v,
            onValueChange = { v = it; if (it >= 0.98f) onComplete() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}
