package lab.p4c.nextup.core.domain.alarm.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.feature.alarm.data.local.dao.AlarmDao
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import javax.inject.Inject

class DeleteAlarmAndCancel @Inject constructor(
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler
) {
    suspend operator fun invoke(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
        scheduler.cancel(id)
    }
}
