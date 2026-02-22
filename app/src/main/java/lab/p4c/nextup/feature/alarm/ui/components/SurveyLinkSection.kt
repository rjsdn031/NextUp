package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.util.clickableThrottle

@Composable
fun SurveyLinkSection(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    val backgroundColor = if (enabled) c.primary else c.surfaceVariant
    val contentColor = if (enabled) c.onPrimary else c.onSurfaceVariant
    Surface(
        color = backgroundColor,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableThrottle(enabled) { onClick() },
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (enabled) "오늘의 설문 작성하기" else "오늘 설문 작성 완료",
                    style = t.titleMedium,
                    color = contentColor
                )
            }
        }
    }
}