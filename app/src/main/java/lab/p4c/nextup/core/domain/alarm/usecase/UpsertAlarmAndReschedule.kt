package lab.p4c.nextup.core.domain.alarm.usecase

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.feature.alarm.data.local.AppDatabase
import lab.p4c.nextup.feature.alarm.data.local.dao.AlarmDao
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import lab.p4c.nextup.feature.alarm.data.mapper.toEntity
import javax.inject.Inject

class UpsertAlarmAndReschedule @Inject constructor(
    private val db: AppDatabase,
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler,
    private val nextTrigger: NextTriggerCalculator
) {
    suspend operator fun invoke(alarm: Alarm) = withContext(Dispatchers.IO) {
        var scheduleId: Int? = null
        var scheduleAt: Long? = null
        var scheduleAlarm: Alarm? = null
        var cancelId: Int? = null

        db.withTransaction {
            val entity = alarm.toEntity()

            // 실제 PK 확보 (id=0 신규 → insert 후 PK 회수, 기존 → update)
            val persistedId: Int = if (entity.id == 0) {
                dao.insert(entity).toInt()
            } else {
                val rows = dao.update(entity)
                if (rows == 0) dao.insert(entity).toInt() else entity.id
            }

            val normalized = alarm.copy(id = persistedId)

            if (normalized.enabled) {
                val nextAt = nextTrigger.computeUtcMillis(normalized)
                scheduleId = persistedId
                scheduleAt = nextAt
                scheduleAlarm = normalized
            } else {
                cancelId = persistedId
            }
        }

        cancelId?.let { scheduler.cancel(it) }
        if (scheduleId != null && scheduleAt != null && scheduleAlarm != null) {
            // 동일 id 재등록 시 깔끔하게
            scheduler.cancel(scheduleId)
            scheduler.schedule(scheduleId, scheduleAt, scheduleAlarm)
        }
    }
}