package lab.p4c.nextup.core.domain.telemetry.port

interface AlarmLoggingWindow {
    fun markAlarmTriggered(timestampMsUtc: Long)
    fun isWithinWindow(nowMsUtc: Long, windowMs: Long = 60 * 60 * 1000L): Boolean
}
