package lab.p4c.nextup.domain.usecase

import java.time.*
import javax.inject.Inject
import lab.p4c.nextup.domain.model.Alarm

class NextTriggerCalculator @Inject constructor(
    private val clock: Clock
) {
    fun computeUtcMillis(alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now(clock)): Long {
        var candidate = now.withHour(alarm.hour).withMinute(alarm.minute)
            .withSecond(0).withNano(0)
        if (candidate.isBefore(now)) candidate = candidate.plusDays(1)
        return candidate.toInstant().toEpochMilli()
    }
}
