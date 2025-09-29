package lab.p4c.nextup.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.domain.model.Alarm
import lab.p4c.nextup.util.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun AlarmListView(
    alarms: List<Alarm>,
    now: Instant,
    onDelete: (Int) -> Unit,
    onUpdate: (Alarm) -> Unit,
    onAdd: (Alarm) -> Unit,
    onToggle: (Alarm, Boolean) -> Unit,
    onTap: (Alarm, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val nowZdt: ZonedDateTime = now.atZone(ZoneId.systemDefault())

    // Flutter와 동일: 활성 알람만 추려 다음 울릴 순서로 정렬
    val enabledAlarms = alarms.filter { it.enabled }.sortedBy {
        val today = nowZdt.withHour(it.hour).withMinute(it.minute).withSecond(0).withNano(0)
        val dt = if (today.isBefore(nowZdt)) today.plusDays(1) else today
        dt.toInstant().toEpochMilli()
    }

    val nextAlarm = enabledAlarms.firstOrNull()
    val nextAlarmMessage = if (nextAlarm != null)
        getTimeUntilAlarm(nextAlarm.hour, nextAlarm.minute, nowZdt)
    else
        "설정된 다음 알람이 없습니다"

    Column(modifier = modifier.fillMaxSize()) {
        AlarmHeader(now = now, nextAlarmMessage = nextAlarmMessage)

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(alarms, key = { _, a -> a.id }) { index, alarm ->
                val timeText = formatTimeOfDay(alarm.hour, alarm.minute)
                val days = alarm.days.map { dayOfWeekToKor(it) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTap(alarm, index) }
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
