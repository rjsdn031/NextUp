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
        var finalId: Int = alarm.id
        var finalModel: Alarm = alarm
        var nextAt: Long? = null

        db.withTransaction {
            val incoming = alarm.toEntity()

            val excludeId = if (incoming.id == 0) -1 else incoming.id
            val dups = dao.findByTimeAndDaysExceptId(
                hour = incoming.hour,
                minute = incoming.minute,
                repeatMask = incoming.repeatMask,
                excludeId = excludeId
            )

            val survivorId = when {
                incoming.id != 0 -> incoming.id
                dups.isNotEmpty() -> dups.first().id
                else -> 0
            }

            val toSave = if (survivorId == 0) incoming else incoming.copy(id = survivorId)
            val persistedId = if (toSave.id == 0) {
                dao.insert(toSave).toInt()
            } else {
                val rows = dao.update(toSave)
                if (rows == 0) dao.insert(toSave).toInt() else toSave.id
            }

            finalId = persistedId
            finalModel = alarm.copy(id = persistedId)

            if (dups.isNotEmpty()) {
                val survivors = setOf(persistedId)
                val toDelete = dups.map { it.id }.filterNot { it in survivors }
                if (toDelete.isNotEmpty()) {
                    dao.deleteByIds(toDelete)
                }
            }

            if (finalModel.enabled) {
                val nowZdt = timeProvider.nowLocal().atZone(ZoneId.systemDefault())
                nextAt = nextTrigger.computeUtcMillis(finalModel, now = nowZdt)
            } else {
                nextAt = null
            }
        }

        scheduler.cancel(finalId)

        if (finalModel.enabled) {
            val nowUtc = System.currentTimeMillis()
            if (nextAt == null || nextAt!! <= nowUtc) {
                Log.w("AlarmScheduler", "nextAt in the past or null: id=$finalId nextAt=$nextAt now=$nowUtc")
                return@withContext
            }
            scheduler.schedule(finalId, nextAt!!, finalModel)
        }
    }
}
