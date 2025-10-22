package lab.p4c.nextup.core.domain.alarm.port

import lab.p4c.nextup.core.domain.alarm.model.Alarm

interface AlarmScheduler {
    fun schedule(id: Int, triggerAtUtcMillis: Long, alarm: Alarm)
    fun cancel(id: Int)
}