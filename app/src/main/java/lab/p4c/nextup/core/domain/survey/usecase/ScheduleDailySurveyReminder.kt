package lab.p4c.nextup.core.domain.survey.usecase

import lab.p4c.nextup.core.domain.survey.port.SurveyReminderScheduler
import lab.p4c.nextup.core.domain.system.TimeProvider
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class ScheduleDailySurveyReminder @Inject constructor(
    private val scheduler: SurveyReminderScheduler,
    private val timeProvider: TimeProvider
) {
    /** 오늘/내일 중 가장 가까운 21:00(저녁 9시)로 예약 */
    operator fun invoke(
        targetTime: LocalTime = LocalTime.of(21, 0),
        zone: ZoneId = ZoneId.systemDefault(),
        equalIsToday: Boolean = false
    ) {
        val now: ZonedDateTime = timeProvider.now().atZone(zone)

        val todayTarget = ZonedDateTime.of(now.toLocalDate(), targetTime, zone)

        val next =
            if (now.isBefore(todayTarget) || (equalIsToday && now.toLocalTime() == targetTime))
                todayTarget
            else
                todayTarget.plusDays(1)

        scheduler.scheduleAt(next)
    }

    operator fun invoke(
        targetHour: Int,
        targetMinute: Int,
        zone: ZoneId = ZoneId.systemDefault(),
        equalIsToday: Boolean = false,
    ) = invoke(LocalTime.of(targetHour, targetMinute), zone, equalIsToday)
}