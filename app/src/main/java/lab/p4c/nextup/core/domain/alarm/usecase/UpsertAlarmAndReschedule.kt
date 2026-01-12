package lab.p4c.nextup.core.domain.alarm.usecase

import android.util.Log
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger
import lab.p4c.nextup.feature.alarm.data.local.AppDatabase
import lab.p4c.nextup.feature.alarm.data.local.dao.AlarmDao
import lab.p4c.nextup.feature.alarm.data.mapper.toEntity
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.floor

class UpsertAlarmAndReschedule @Inject constructor(
    private val db: AppDatabase,
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler,
    private val nextTrigger: NextTriggerCalculator,
    private val timeProvider: TimeProvider,
    private val telemetryLogger: TelemetryLogger,
) {
    suspend operator fun invoke(alarm: Alarm) = withContext(Dispatchers.IO) {
        var finalId: Int = alarm.id
        var finalModel: Alarm = alarm
        var nextAt: Long? = null
        var persistedByInsert = false

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
                persistedByInsert = true
                dao.insert(toSave).toInt()
            } else {
                val rows = dao.update(toSave)
                if (rows == 0) {
                    persistedByInsert = true
                    dao.insert(toSave).toInt()
                } else {
                    toSave.id
                }
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

            nextAt = if (finalModel.enabled) {
                val nowZdt = timeProvider.nowLocal().atZone(ZoneId.systemDefault())
                nextTrigger.computeUtcMillis(finalModel, now = nowZdt)
            } else {
                null
            }
        }

        runCatching {
            // AlarmCreated / AlarmUpdated 이벤트 기록
            val eventName = if (persistedByInsert) "AlarmCreated" else "AlarmUpdated"
            telemetryLogger.log(
                eventName = eventName,
                payload = buildAlarmPayload(
                    alarmId = finalId,
                    alarm = finalModel,
                    nextTriggerUtcMillis = nextAt
                )
            )
        }.onFailure { Log.w("Telemetry", "Alarm event log failed", it) }

        scheduler.cancel(finalId)

        if (finalModel.enabled) {
            val nowUtc = System.currentTimeMillis()
            val at = nextAt
            if (at == null || at <= nowUtc) {
                Log.w(
                    "AlarmScheduler",
                    "nextAt in the past or null: id=$finalId nextAt=$nextAt now=$nowUtc"
                )
                return@withContext
            }
            scheduler.schedule(finalId, at, finalModel)
        }
    }
}

private fun buildAlarmPayload(
    alarmId: Int,
    alarm: Alarm,
    nextTriggerUtcMillis: Long?,
): Map<String, String> {

    val timeLocal = "%02d:%02d".format(alarm.hour, alarm.minute)
    val repeatDays = alarm.days.joinToString(",") { it.name }

    return mapOf(
        "AlarmId" to alarmId.toString(),

        "AlarmTimeLocal" to timeLocal,
        "RepeatDays" to repeatDays,
        "NextTriggerUtc" to (nextTriggerUtcMillis?.toString() ?: ""),

        "AlarmName" to alarm.name,
        "SoundType" to alarm.sound.toSoundType(),
        "SoundName" to alarm.sound.toSoundName(),
        "Volume" to (floor(alarm.volume * 100) / 100).toString(),

        "SnoozeEnable" to alarm.snoozeEnabled.toString(),
        "SnoozeInterval" to alarm.snoozeInterval.toString(),
        "SnoozeMaxCount" to alarm.maxSnoozeCount.toString()
    )
}

private fun AlarmSound.toSoundType(): String = when (this) {
    is AlarmSound.Asset -> "Asset"
    is AlarmSound.System -> "System"
    is AlarmSound.Custom -> "Custom"
}

private fun AlarmSound.toSoundName(): String = when (this) {
    is AlarmSound.Asset -> resName
    is AlarmSound.System -> uri
    is AlarmSound.Custom -> uri
}
