package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.core.common.time.indexToStr

/**
 * repeatDays: 1=Mon ... 7=Sun
 */
@Composable
fun DaySelector(
    selectedDays: List<Int>,
    onChange: (List<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf(1, 2, 3, 4, 5, 6, 7)

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { d ->
            val selected = d in selectedDays

            DayChip(
                text = d.indexToStr(),
                selected = selected,
                onClick = {
                    onChange(
                        if (selected) selectedDays - d else selectedDays + d
                    )
                }
            )
        }
    }
}