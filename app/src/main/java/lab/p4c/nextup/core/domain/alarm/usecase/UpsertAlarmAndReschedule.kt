package lab.p4c.nextup.core.domain.alarm.usecase

import android.util.Log
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.feature.alarm.data.local.AppDatabase
import lab.p4c.nextup.feature.alarm.data.local.dao.AlarmDao
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.feature.alarm.data.mapper.toEntity
import java.time.ZoneId
import javax.inject.Inject

class UpsertAlarmAndReschedule @Inject constructor(
    private val db: AppDatabase,
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler,
    private val nextTrigger: NextTriggerCalculator,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(alarm: Alarm) = withContext(Dispatchers.IO) {
        var persistedId: Int? = null
        var normalized: Alarm? = null

        db.withTransaction {
            val entity = alarm.toEntity()
            val id = if (entity.id == 0) {
                dao.insert(entity).toInt()
            } else {
                val rows = dao.update(entity)
                if (rows == 0) dao.insert(entity).toInt() else entity.id
            }
            persistedId = id
            normalized = alarm.copy(id = id)
        }

        val id = requireNotNull(persistedId)
        val model = requireNotNull(normalized)

        if (!model.enabled) {
            scheduler.cancel(id)
            return@withContext
        }

        // 동일 기준시간으로 계산
        val nowZdt = timeProvider.nowLocal().atZone(ZoneId.systemDefault())
        val nextAt = nextTrigger.computeUtcMillis(model, now = nowZdt)

        // 방어 로직: nextAt 유효성 체크
        val nowUtc = java.time.Instant.now().toEpochMilli()
        if (nextAt <= nowUtc) {

            scheduler.cancel(id)
             Log.w("AlarmScheduler","nextAt in the past: id=$id nextAt=$nextAt now=$nowUtc")
            return@withContext
        }

        // 동일 id 재등록 시 깔끔하게
        scheduler.cancel(id)
        scheduler.schedule(id, nextAt, model)
    }
}