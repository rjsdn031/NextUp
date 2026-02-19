package lab.p4c.nextup.core.domain.alarm.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler

private const val MANDATORY_ALARM_ID = 1

/**
 * Deletes an alarm and cancels any scheduled trigger associated with it.
 *
 * Policy:
 * - The mandatory alarm (currently [MANDATORY_ALARM_ID]) cannot be deleted.
 *
 * Notes:
 * - Cancellation is performed regardless of whether the alarm existed in persistence,
 *   to ensure scheduling consistency.
 *
 * TODO(policy):
 * - Avoid hard-coding mandatory alarm identity by id. Consider explicit flag/config.
 */
class DeleteAlarmAndCancel @Inject constructor(
    private val repo: AlarmRepository,
    private val scheduler: AlarmScheduler,
) {
    suspend operator fun invoke(id: Int) = withContext(Dispatchers.IO) {
        if (id == MANDATORY_ALARM_ID) return@withContext

        repo.delete(id)
        scheduler.cancel(id)
    }
}