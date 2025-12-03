package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.time.ZonedDateTime

@Composable
fun AlarmListMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateUsage: () -> Unit,
    onTestSurveyReminder: (ZonedDateTime) -> Unit,
    now: ZonedDateTime
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography
    val ctx = LocalContext.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("설정", style = t.bodyLarge, color = c.onSurface) },
            onClick = {
                onDismiss()
                onNavigateSettings()
            },
            colors = MenuDefaults.itemColors()
        )

        DropdownMenuItem(
            text = { Text("앱 사용 통계", style = t.bodyLarge, color = c.onSurface) },
            onClick = {
                onDismiss()
                onNavigateUsage()
            },
            colors = MenuDefaults.itemColors()
        )

        DropdownMenuItem(
            text = { Text("푸시알림 테스트", style = t.bodyLarge, color = c.onSurface) },
            onClick = {
                onDismiss()
                onTestSurveyReminder(now)
            },
            colors = MenuDefaults.itemColors()
        )
    }
}