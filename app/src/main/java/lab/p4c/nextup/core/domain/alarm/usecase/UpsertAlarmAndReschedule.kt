package lab.p4c.nextup.core.domain.alarm.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.floor

/**
 * Upserts an [Alarm] configuration and reschedules its next system trigger.
 *
 * Contract:
 * - Persistence (including deduplication and id assignment) is delegated to [AlarmRepository].
 * - If the persisted alarm is enabled, compute the next trigger instant via [NextTriggerCalculator].
 * - Cancel any previous schedule for the alarm id, then schedule the computed next trigger.
 * - Emit a telemetry event: "AlarmCreated" or "AlarmUpdated".
 *
 * Time contract:
 * - Scheduling uses UTC epoch millis returned from [NextTriggerCalculator.computeUtcMillis].
 *
 * Notes:
 * - If the computed next trigger is null or not strictly in the future, scheduling is skipped.
 */
class UpsertAlarmAndReschedule @Inject constructor(
    private val repo: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val nextTrigger: NextTriggerCalculator,
    private val timeProvider: TimeProvider,
    private val telemetryLogger: TelemetryLogger,
) {
    suspend operator fun invoke(alarm: Alarm) = withContext(Dispatchers.IO) {
        val result = repo.upsert(alarm)
        val persisted = result.alarm

        val nextAtUtcMillis = if (persisted.enabled) {
            val nowZdt = timeProvider.nowLocal().atZone(ZoneId.systemDefault())
            nextTrigger.computeUtcMillis(persisted, now = nowZdt)
        } else {
            null
        }

        runCatching {
            telemetryLogger.log(
                eventName = if (result.created) "AlarmCreated" else "AlarmUpdated",
                payload = buildAlarmPayload(
                    alarmId = persisted.id,
                    alarm = persisted,
                    nextTriggerUtcMillis = nextAtUtcMillis
                )
            )
        }

        // Always clear any previous schedule for consistency.
        scheduler.cancel(persisted.id)

        if (!persisted.enabled) return@withContext

        val at = nextAtUtcMillis ?: return@withContext

        // TODO(refactor): Prefer a TimeProvider-based UTC millis API for testability.
        if (at <= System.currentTimeMillis()) return@withContext

        scheduler.schedule(persisted.id, at, persisted)
    }
}

/**
 * Builds a stable, non-localized telemetry payload for alarm upsert events.
 *
 * This function is intended for telemetry/logging only and should not be used as a UI formatter.
 */
// TODO(refactor): Move alarm telemetry payload mapping into a dedicated telemetry mapper.
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
