package lab.p4c.nextup.data.scheduler

import lab.p4c.nextup.domain.model.Alarm

interface AlarmScheduler {
    fun schedule(id: Int, triggerAtUtcMillis: Long, alarm: Alarm)
    fun cancel(id: Int)
}
