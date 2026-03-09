package lab.p4c.nextup.core.domain.survey.usecase

import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import lab.p4c.nextup.core.domain.system.TimeProvider

/**
 * 디버그 환경에서 survey reminder 시간을 임시로 구성해
 * [CheckAndRescheduleSurveyReminder]의 실제 정책 흐름을 테스트한다.
 *
 * 이 유즈케이스는 예약 정책을 직접 구현하지 않는다.
 * 항상 [CheckAndRescheduleSurveyReminder]를 통해 다음 reminder를 계산하고 예약한다.
 */
class DebugScheduleSurveyReminder @Inject constructor(
    private val timeProvider: TimeProvider,
    private val checkAndRescheduleSurveyReminder: CheckAndRescheduleSurveyReminder,
) {
    suspend fun scheduleInMinutes(
        offsetsInMinutes: List<Long>,
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<LocalTime> {
        val reminderTimes = buildReminderTimes(offsetsInMinutes, zone)

        checkAndRescheduleSurveyReminder(
            reminderTimes = reminderTimes,
            zone = zone,
        )

        return reminderTimes
    }

    suspend fun scheduleInMinute(
        offsetInMinutes: Long = 1,
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<LocalTime> {
        return scheduleInMinutes(
            offsetsInMinutes = listOf(offsetInMinutes),
            zone = zone,
        )
    }

    private fun buildReminderTimes(
        offsetsInMinutes: List<Long>,
        zone: ZoneId,
    ): List<LocalTime> {
        val now = timeProvider.now()
            .atZone(zone)
            .withSecond(0)
            .withNano(0)

        return offsetsInMinutes
            .distinct()
            .sorted()
            .map { offset -> now.plusMinutes(offset).toLocalTime() }
    }
}