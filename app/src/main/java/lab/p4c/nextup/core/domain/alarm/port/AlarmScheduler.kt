package lab.p4c.nextup.core.domain.alarm.port

import lab.p4c.nextup.core.domain.alarm.model.Alarm

/**
 * Port for scheduling and cancelling alarm triggers at the system level.
 *
 * This interface defines a boundary between domain logic and platform-specific
 * scheduling mechanisms (e.g., Android AlarmManager).
 *
 * Time contract:
 * - [triggerAtUtcMillis] must represent an absolute UTC epoch timestamp.
 * - Implementations are responsible for converting to platform-specific APIs.
 *
 * Identity contract:
 * - [id] must correspond to [Alarm.id].
 * - Re-scheduling with the same [id] should replace any existing scheduled trigger.
 *
 * Idempotency:
 * - Calling [cancel] on a non-existing schedule should be treated as a no-op.
 */
interface AlarmScheduler {

    /**
     * Schedules a system trigger for the given alarm.
     *
     * @param id Unique identifier of the alarm.
     * @param triggerAtUtcMillis Absolute UTC timestamp (epoch millis).
     * @param alarm Alarm configuration at scheduling time.
     *
     * The [alarm] object is provided for implementation-level needs
     * (e.g., passing configuration into PendingIntent or metadata).
     */
    fun schedule(id: Int, triggerAtUtcMillis: Long, alarm: Alarm)   // TODO(refactor): [Alarm]을 통째로 밷아야 하는지 의문

    /**
     * Cancels any scheduled trigger associated with the given [id].
     */
    fun cancel(id: Int)
}
