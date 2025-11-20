package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DayChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme

    Surface(
        shape = CircleShape,
        color = if (selected) c.primaryContainer else c.surfaceVariant,
        contentColor = if (selected) c.onPrimaryContainer else c.onSurfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) c.primary else c.outline
        ),
        modifier = modifier
            .widthIn(min = 36.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
