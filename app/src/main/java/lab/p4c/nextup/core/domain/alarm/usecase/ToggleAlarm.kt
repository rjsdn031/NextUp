package lab.p4c.nextup.core.domain.alarm.usecase

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.feature.alarm.data.local.AppDatabase
import lab.p4c.nextup.feature.alarm.data.local.dao.AlarmDao
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import lab.p4c.nextup.feature.alarm.data.mapper.toDomain
import javax.inject.Inject


private const val MANDATORY_ALARM_ID = 1

class ToggleAlarm @Inject constructor(
    private val db: AppDatabase,
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler,
    private val nextTrigger: NextTriggerCalculator
) {
    private sealed interface SchedulerAction {
        data class Schedule(val id: Int, val at: Long, val alarm: Alarm) : SchedulerAction
        data class Cancel(val id: Int) : SchedulerAction
        data object None : SchedulerAction
    }

    suspend operator fun invoke(id: Int, enabled: Boolean) =
        withContext(Dispatchers.IO) {

            if (id == MANDATORY_ALARM_ID && !enabled) return@withContext

            val action = db.withTransaction {
                dao.updateEnabled(id, enabled)

                val entity = dao.findByIdOrNull(id) ?: return@withTransaction SchedulerAction.None
                val alarm = entity.toDomain()

                if (enabled) {
                    val nextAt = nextTrigger.computeUtcMillis(alarm)
                    SchedulerAction.Schedule(id, nextAt, alarm)
                } else {
                    SchedulerAction.Cancel(id)
                }
            }

            when (action) {
                is SchedulerAction.Schedule ->
                    scheduler.schedule(action.id, action.at, action.alarm)

                is SchedulerAction.Cancel ->
                    scheduler.cancel(action.id)

                SchedulerAction.None -> Unit
            }
        }
}