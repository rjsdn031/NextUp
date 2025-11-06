package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.p4c.nextup.core.common.time.indexToStr

/**
 * repeatDays: 1=월 ... 7=일
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DaySelector(
    selectedDays: List<Int>,
    onChange: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography
    val days = listOf(1, 2, 3, 4, 5, 6, 7)

    FlowRow(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        days.forEach { d ->
            val selected = d in selectedDays

            FilterChip(
                selected = selected,
                onClick = {
                    onChange(if (selected) selectedDays - d else selectedDays + d)
                },
                label = {
                    Text(
                        text = d.indexToStr(),
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        style = t.labelLarge
                    )
                },
                shape = CircleShape,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    selectedBorderColor = c.primary,
                    borderColor = c.outline
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = c.surfaceVariant,
                    labelColor = c.onSurfaceVariant,
                    selectedContainerColor = c.primaryContainer,
                    selectedLabelColor = c.onPrimaryContainer,
                    iconColor = c.onSurfaceVariant,
                    selectedLeadingIconColor = c.onPrimaryContainer
                ),
                modifier = Modifier
                    .widthIn(min = 40.dp)
            )
        }
    }
}
