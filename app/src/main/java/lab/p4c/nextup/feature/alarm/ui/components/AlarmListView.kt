package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.util.clickableThrottle
import lab.p4c.nextup.core.common.time.dayOfWeekToKor
import lab.p4c.nextup.core.common.time.formatTimeOfDay
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import java.time.ZonedDateTime

@Composable
fun AlarmListView(
    alarms: List<Alarm>,
    now: ZonedDateTime,
    onDelete: (Int) -> Unit,
    onUpdate: (Alarm) -> Unit,
    onAdd: (Alarm) -> Unit,
    onToggle: (Alarm, Boolean) -> Unit,
    onTap: (Alarm, Int) -> Unit,
    computeNextMillis: (Alarm, ZonedDateTime) -> Long,
    formatNext: (Long) -> String,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme

    val nextAlarmMessage = remember(alarms, now) {
        val nextMillis = alarms
            .asSequence()
            .filter { it.enabled }
            .map { computeNextMillis(it, now) }
            .minOrNull()

        nextMillis?.let { formatNext(it) } ?: "설정된 다음 알람이 없습니다"
    }

    Surface(
        color = c.background,
        contentColor = c.onBackground,
        modifier = modifier.fillMaxSize()
    ) {
        Column(Modifier.fillMaxSize()) {
            AlarmHeader(now = now, nextAlarmMessage = nextAlarmMessage)

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(alarms, key = { _, a -> a.id }) { index, alarm ->
                    val timeText = formatTimeOfDay(alarm.hour, alarm.minute)
                    val days = alarm.days.map { dayOfWeekToKor(it) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableThrottle { onTap(alarm, index) }
                        ) {
                            AlarmTile(
                                time = timeText,
                                days = days,
                                enabled = alarm.enabled,
                                onToggle = { checked -> onToggle(alarm, checked) }
                            )
                        }
                }
            }
        }
    }
}