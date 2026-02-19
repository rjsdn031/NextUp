package lab.p4c.nextup.core.domain.alarm.usecase

import java.time.ZoneId
import javax.inject.Inject
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import lab.p4c.nextup.core.domain.system.TimeProvider

/**
 * Handles user dismissal of a ringing alarm.
 *
 * Contract:
 * - This use case should be invoked only after the alarm has actually triggered and the user dismissed it.
 *
 * Policy:
 * - One-time alarm ([Alarm.days] is empty): disable it and cancel any scheduled trigger.
 * - Repeating alarm: keep it enabled and schedule the next occurrence.
 *
 * Time contract:
 * - The next trigger is computed as UTC epoch millis via [NextTriggerCalculator].
 */
class DismissAlarm @Inject constructor(
    private val repo: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val nextTriggerCalculator: NextTriggerCalculator,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(alarmId: Int) {
        val alarm = repo.getById(alarmId) ?: return

        // One-time alarm: disable and cancel schedule.
        if (alarm.days.isEmpty()) {
            repo.setEnabled(alarmId, false)
            scheduler.cancel(alarmId)
            return
        }

        // Repeating alarm: reschedule to the next occurrence.
        val nowZdt = timeProvider.nowLocal().atZone(ZoneId.systemDefault())
        val nextUtcMillis = nextTriggerCalculator.computeUtcMillis(alarm, now = nowZdt)

        scheduler.cancel(alarmId)
        scheduler.schedule(alarmId, nextUtcMillis, alarm)
    }
}
