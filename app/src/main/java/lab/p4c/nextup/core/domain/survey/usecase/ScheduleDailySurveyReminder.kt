package lab.p4c.nextup.core.domain.survey.usecase

import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.first
import lab.p4c.nextup.core.domain.experiment.usecase.IsExperimentActive
import lab.p4c.nextup.core.domain.survey.port.SurveyReminderScheduler
import lab.p4c.nextup.core.domain.system.TimeProvider
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Schedules a single survey reminder at the nearest target time (today or tomorrow).
 *
 * NOTE:
 * This use case is kept for manual/debug scheduling only.
 * Production scheduling should be handled by [CheckAndRescheduleSurveyReminder],
 * which applies the full policy (experiment active, daily limit, etc.).
 */
@Deprecated(
    message = "Use CheckAndRescheduleSurveyReminder for policy-based scheduling. " +
            "This API is for manual/debug scheduling only.",
    replaceWith = ReplaceWith("checkAndReschedule()")
)
class ScheduleDailySurveyReminder @Inject constructor(
    private val scheduler: SurveyReminderScheduler,
    private val timeProvider: TimeProvider,
    private val isExperimentActive: IsExperimentActive,
) {
    suspend operator fun invoke(
        targetTime: LocalTime = LocalTime.of(19, 0),
        zone: ZoneId = ZoneId.systemDefault(),
        equalIsToday: Boolean = false
    ) {
        val active = runCatching {
            withTimeout(1500) { isExperimentActive().first() }
        }.getOrDefault(false)

        if (!active) {
            scheduler.cancel()
            return
        }

        val now: ZonedDateTime = timeProvider.now().atZone(zone)
        val todayTarget = ZonedDateTime.of(now.toLocalDate(), targetTime, zone)

        val next =
            if (now.isBefore(todayTarget) || (equalIsToday && now.toLocalTime() == targetTime))
                todayTarget
            else
                todayTarget.plusDays(1)

        scheduler.cancel()
        scheduler.scheduleAt(next)
    }

    suspend operator fun invoke(
        targetHour: Int,
        targetMinute: Int,
        zone: ZoneId = ZoneId.systemDefault(),
        equalIsToday: Boolean = false,
    ) = invoke(LocalTime.of(targetHour, targetMinute), zone, equalIsToday)
}