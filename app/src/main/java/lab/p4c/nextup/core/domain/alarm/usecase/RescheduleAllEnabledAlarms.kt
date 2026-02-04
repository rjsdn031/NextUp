package lab.p4c.nextup.core.domain.alarm.usecase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.feature.alarm.data.local.dao.AlarmDao
import lab.p4c.nextup.feature.alarm.data.mapper.toDomain // 없으면 새로 만들기
import java.time.ZoneId
import javax.inject.Inject

class RescheduleAllEnabledAlarms @Inject constructor(
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler,
    private val nextTrigger: NextTriggerCalculator,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val enabled = dao.findEnabledAll()
        if (enabled.isEmpty()) {
            Log.d(TAG, "No enabled alarms to reschedule")
            return@withContext
        }

        val nowZdt = timeProvider.nowLocal().atZone(ZoneId.systemDefault())

        enabled.forEach { e ->
            val alarm = e.toDomain()

            scheduler.cancel(alarm.id)

            val nextAt = nextTrigger.computeUtcMillis(alarm, now = nowZdt)
            val nowUtc = System.currentTimeMillis()
            if (nextAt <= nowUtc) {
                Log.w(TAG, "Skip past trigger id=${alarm.id} nextAt=$nextAt now=$nowUtc")
                return@forEach
            }

            scheduler.schedule(alarm.id, nextAt, alarm)
            Log.d(TAG, "Rescheduled id=${alarm.id} at=$nextAt")
        }
    }

    private companion object {
        private const val TAG = "RescheduleAlarms"
    }
}
