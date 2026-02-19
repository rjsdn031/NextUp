package lab.p4c.nextup.core.domain.alarm.usecase

import java.time.ZoneId
import javax.inject.Inject
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import lab.p4c.nextup.core.domain.system.TimeProvider

private const val MANDATORY_ALARM_ID = 1

/**
 * Toggles enabled state of an alarm and updates its scheduled trigger accordingly.
 *
 * Policy:
 * - Certain alarms may not be disabled (currently identified by [MANDATORY_ALARM_ID]).
 *
 * Scheduling:
 * - When enabled is set to true, schedule the next trigger based on current time.
 * - When enabled is set to false, cancel any scheduled trigger.
 *
 * TODO(policy):
 * - Avoid hard-coding mandatory alarm identity by id. Consider explicit flag/config.
 */
class ToggleAlarm @Inject constructor(
    private val repo: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val nextTrigger: NextTriggerCalculator,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(id: Int, enabled: Boolean) {
        // Mandatory alarm cannot be disabled; ignore the request.
        if (id == MANDATORY_ALARM_ID && !enabled) return

        val updated = repo.setEnabledAndGet(id, enabled) ?: return

        // Always clear existing schedule first for consistency.
        scheduler.cancel(id)

        // Use persisted state for correctness (repository may enforce additional policies).
        if (!updated.enabled) return

        val nowZdt = timeProvider.nowLocal().atZone(ZoneId.systemDefault())
        val nextAtUtcMillis = nextTrigger.computeUtcMillis(updated, now = nowZdt)

        // TODO(refactor): Prefer a TimeProvider-based UTC millis API for testability.
        if (nextAtUtcMillis <= System.currentTimeMillis()) return

        scheduler.schedule(id, nextAtUtcMillis, updated)
    }
}
