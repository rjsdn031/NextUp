package lab.p4c.nextup.domain.usecase

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.data.local.AppDatabase
import lab.p4c.nextup.data.local.dao.AlarmDao
import lab.p4c.nextup.data.mapper.toDomain
import lab.p4c.nextup.data.scheduler.AlarmScheduler
import javax.inject.Inject

class ToggleAlarm @Inject constructor(
    private val db: AppDatabase,
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler,
    private val nextTrigger: NextTriggerCalculator
) {
    suspend operator fun invoke(id: Int, enabled: Boolean) = withContext(Dispatchers.IO) {
        var post: () -> Unit = {}
        db.withTransaction {
            dao.updateEnabled(id, enabled)

            val entity = dao.findByIdOrNull(id) ?: return@withTransaction
            val alarm = entity.toDomain()

            if (enabled) {
                val nextAt = nextTrigger.computeUtcMillis(alarm)
                post = { scheduler.schedule(id, nextAt, alarm) }
            } else {
                post = { scheduler.cancel(id) }
            }
        }

        post()
    }
}
