package lab.p4c.nextup.core.domain.survey.usecase

import android.util.Log
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import lab.p4c.nextup.core.domain.survey.port.SurveyReminderScheduler
import lab.p4c.nextup.core.domain.survey.port.SurveyRepository
import lab.p4c.nextup.core.domain.system.TimeProvider

/**
 * 오늘 설문 제출 여부를 기준으로 다음 리마인더를 재예약한다.
 *
 * 동작 방식:
 * - reminder 시간이 비어 있으면 예약을 취소한다.
 * - 오늘 설문이 이미 제출되었으면 내일 첫 reminder 시각으로 예약한다.
 * - 아직 제출되지 않았으면 오늘 남은 reminder 중 가장 가까운 시각으로 예약한다.
 * - 오늘 남은 reminder 시간이 없으면 다음 날 첫 reminder 시각으로 예약한다.
 */
class CheckAndRescheduleSurveyReminder @Inject constructor(
    private val repo: SurveyRepository,
    private val scheduler: SurveyReminderScheduler,
    private val timeProvider: TimeProvider,
) {
    sealed interface SurveyReminderRescheduleResult {
        data class Scheduled(val at: ZonedDateTime) : SurveyReminderRescheduleResult
        data object EmptyReminderTimes : SurveyReminderRescheduleResult
    }

    suspend operator fun invoke(
        reminderTimes: List<LocalTime> = DEFAULT_REMINDER_TIMES,
        zone: ZoneId = ZoneId.systemDefault(),
    ): SurveyReminderRescheduleResult {
        val sortedTimes = reminderTimes.distinct().sorted()

        if (sortedTimes.isEmpty()) {
            scheduler.cancel()
            Log.w(TAG, "Reminder times are empty. Survey reminder cancelled.")
            return SurveyReminderRescheduleResult.EmptyReminderTimes
        }

        val now = timeProvider.now().atZone(zone)
        val today = now.toLocalDate()
        val todayKey = today.toSurveyDateKey()

        val alreadySubmitted = try {
            repo.getByDate(todayKey) != null
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Failed to read survey for date=$todayKey. Continue as not submitted.",
                e,
            )
            false
        }

        val next = if (alreadySubmitted) {
            ZonedDateTime.of(
                today.plusDays(1),
                sortedTimes.first(),
                zone,
            )
        } else {
            val nextToday = sortedTimes
                .asSequence()
                .map { reminderTime -> ZonedDateTime.of(today, reminderTime, zone) }
                .firstOrNull { scheduledAt -> scheduledAt.isAfter(now) }

            nextToday
                ?: ZonedDateTime.of(
                    today.plusDays(1),
                    sortedTimes.first(),
                    zone,
                )
        }

        scheduler.cancel()
        scheduler.scheduleAt(next)

        Log.d(
            TAG,
            "Survey reminder rescheduled. todayKey=$todayKey, alreadySubmitted=$alreadySubmitted, next=$next",
        )

        return SurveyReminderRescheduleResult.Scheduled(next)
    }

    private fun LocalDate.toSurveyDateKey(): String = toString()

    companion object {
        private const val TAG = "SurveyReminder"

        private val DEFAULT_REMINDER_TIMES = listOf(
            LocalTime.of(19, 0),
            LocalTime.of(21, 0),
            LocalTime.of(23, 0),
            LocalTime.of(23, 59),
        )
    }
}