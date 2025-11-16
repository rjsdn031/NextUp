package lab.p4c.nextup.feature.usage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.feature.usage.ui.model.AppUsageRow
import java.util.concurrent.TimeUnit

@Composable
fun UsageRowItem(
    row: AppUsageRow,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme

    Surface(
        color = c.surface,
        contentColor = c.onSurface,
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    row.packageName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "총 사용 시간: ${formatDuration(row.totalMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.onSurfaceVariant
                )
            }
        }
    }
}


private fun formatDuration(millis: Long): String {
    val totalSec = TimeUnit.MILLISECONDS.toSeconds(millis)
    val minutes = totalSec / 60
    val seconds = (totalSec % 60)
    return "${minutes}분 ${seconds}초"
}
