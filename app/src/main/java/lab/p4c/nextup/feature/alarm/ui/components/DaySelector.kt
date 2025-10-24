package lab.p4c.nextup.feature.alarm.ui.components

import android.R.attr.enabled
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.p4c.nextup.core.common.time.indexToStr

/**
 * repeatDays: 1=월 ... 7=일
 */
@Composable
fun DaySelector(
    selectedDays: List<Int>,
    onChange: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = listOf(1, 2, 3, 4, 5, 6, 7)

    Column(modifier) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 4.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            days.forEach { d ->
                val on = d in selectedDays
                FilterChip(
                    selected = on,
                    onClick = {
                        onChange(if (on) selectedDays - d else selectedDays + d)
                    },
                    label = {
                        Text(
                            text = d.indexToStr(),
                            fontSize = 14.sp,
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                            color = if (on)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp)
                        )
                    },
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = on,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = CircleShape,
//                    modifier = Modifier
//                        .padding(2.dp)
//                        .widthIn(min = 28.dp)
                )
            }
        }
    }
}


