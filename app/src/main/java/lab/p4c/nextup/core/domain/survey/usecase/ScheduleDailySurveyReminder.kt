package lab.p4c.nextup.core.domain.survey.usecase

import lab.p4c.nextup.core.domain.survey.port.SurveyReminderScheduler
import lab.p4c.nextup.core.domain.system.TimeProvider
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class ScheduleDailySurveyReminder @Inject constructor(
    private val scheduler: SurveyReminderScheduler,
    private val timeProvider: TimeProvider
) {
    /** 오늘/내일 중 가장 가까운 21:00(저녁 9시)로 예약 */
    operator fun invoke(targetHour: Int = 21, targetMinute: Int = 0) {
        val now = timeProvider.now().atZone(ZoneId.systemDefault())
        val todayTarget = now.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0)
        val next = if (now.isBefore(todayTarget)) todayTarget else todayTarget.plusDays(1)
        scheduler.scheduleAt(next)
    }
}