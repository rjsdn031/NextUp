package lab.p4c.nextup.feature.alarm.ui.ringing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.theme.NextUpThemeTokens
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
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography
    val x = NextUpThemeTokens.colors

    val now = remember { LocalDateTime.now() }
    val timeStr = now.format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA))
    val dateStr = now.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREA))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(88.dp))

            Text(
                text = timeStr,
                style = t.displayLarge,
                color = c.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = dateStr,
                style = t.titleMedium,
                color = x.textSecondary,
                textAlign = TextAlign.Center
            )

            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = t.headlineSmall,
                    color = c.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            if (body.isNotBlank()) {
                Text(
                    text = body,
                    style = t.titleMedium,
                    color = x.textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.weight(1f))

            DismissSlider(onComplete = onDismiss)

            Spacer(Modifier.height(88.dp))
        }

        if (showSnooze) {
            Button(
                onClick = onSnooze,
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.primaryContainer,
                    contentColor = c.onPrimaryContainer
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
            ) {
                Text("${snoozeMinutes}분 후 다시 알림", style = t.titleMedium)
            }
        }
    }
}
