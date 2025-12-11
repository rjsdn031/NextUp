package lab.p4c.nextup.core.domain.alarm.usecase

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.feature.alarm.data.local.AppDatabase
import lab.p4c.nextup.feature.alarm.data.local.dao.AlarmDao
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import lab.p4c.nextup.feature.alarm.data.mapper.toDomain
import javax.inject.Inject


private const val MANDATORY_ALARM_ID = 1

class ToggleAlarm @Inject constructor(
    private val db: AppDatabase,
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler,
    private val nextTrigger: NextTriggerCalculator
) {
    suspend operator fun invoke(id: Int, enabled: Boolean) = withContext(Dispatchers.IO) {

        if (id == MANDATORY_ALARM_ID && !enabled) {
            return@withContext
        }

        var post: () -> Unit = {}
        db.withTransaction {
            dao.updateEnabled(id, enabled)

            val entity = dao.findByIdOrNull(id) ?: return@withTransaction
            val alarm = entity.toDomain()

            if (enabled) {
                val nextAt = nextTrigger.computeUtcMillis(alarm)
                post = { scheduler.schedule(id, nextAt, alarm) }
            } else {
                post = { scheduler.cancel(id) }
            }
        }

        post()
    }
}