package lab.p4c.nextup.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * repeatDays: 1=월 ... 7=일
 */
@Composable
fun DaySelector(
    selectedDays: List<Int>,
    onChange: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "반복 요일"
) {
    val days = listOf(1,2,3,4,5,6,7)

    Column(modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEach { d ->
                val on = d in selectedDays
                FilterChip(
                    selected = on,
                    onClick = {
                        onChange(if (on) selectedDays - d else selectedDays + d)
                    },
                    label = { Text(dayLabel(d)) }
                )
            }
        }
    }
}

private fun dayLabel(d: Int): String = when(d) {
    1 -> "월"; 2 -> "화"; 3 -> "수"; 4 -> "목"; 5 -> "금"; 6 -> "토"; else -> "일"
}
