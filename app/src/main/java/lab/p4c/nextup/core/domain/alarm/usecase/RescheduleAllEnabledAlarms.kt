package lab.p4c.nextup.core.domain.alarm.usecase

import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import lab.p4c.nextup.core.domain.system.TimeProvider

/**
 * Reschedules all currently enabled alarms.
 *
 * Intended usage:
 * - After reboot / app upgrade / time zone change / exact alarm permission changes,
 *   to restore system-level schedules from persisted configurations.
 *
 * Behavior:
 * - For each enabled alarm, cancel any existing schedule and schedule the next occurrence.
 * - If the computed next trigger is not strictly in the future, scheduling is skipped.
 *
 * Notes:
 * - This use case is best-effort: it attempts to reschedule all alarms even if some fail.
 */
class RescheduleAllEnabledAlarms @Inject constructor(
    private val repo: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val nextTrigger: NextTriggerCalculator,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val enabledAlarms = repo.getEnabledAll()
        if (enabledAlarms.isEmpty()) return@withContext

        val nowZdt = timeProvider.nowLocal().atZone(ZoneId.systemDefault())
        val nowUtcMillis = System.currentTimeMillis() // TODO(refactor): prefer TimeProvider utc API.

        enabledAlarms.forEach { alarm ->
            scheduler.cancel(alarm.id)

            val nextAt = nextTrigger.computeUtcMillis(alarm, now = nowZdt)
            if (nextAt <= nowUtcMillis) return@forEach

            scheduler.schedule(alarm.id, nextAt, alarm)
        }
    }
}
