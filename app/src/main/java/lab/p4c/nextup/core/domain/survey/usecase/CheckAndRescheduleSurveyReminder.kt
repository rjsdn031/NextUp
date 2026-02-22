package lab.p4c.nextup.core.domain.survey.usecase

import kotlinx.coroutines.flow.first
import lab.p4c.nextup.core.domain.experiment.usecase.IsExperimentActive
import lab.p4c.nextup.core.domain.survey.port.SurveyReminderScheduler
import lab.p4c.nextup.core.domain.survey.port.SurveyRepository
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.system.sessionKey
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * 기존 예약된 알림을 기반으로,
 * - 오늘(세션)의 설문 완료 여부 확인 후
 * - 미완료인 경우 다음 예정된 시간으로 재예약한다.
 *
 * Note: 이 유즈케이스는 오직 SurveyReminderReceiver 에서 호출된다.
 *       알림 클릭 시에는 onReceive가 호출되지 않으므로 이 로직은 반복 알림 전용이다.
 */
class CheckAndRescheduleSurveyReminder @Inject constructor(
    private val repo: SurveyRepository,
    private val scheduler: SurveyReminderScheduler,
    private val timeProvider: TimeProvider,
    private val isExperimentActive: IsExperimentActive,
) {
    suspend operator fun invoke(
        reminderTimes: List<LocalTime> = DEFAULT_REMINDER_TIMES,
        zone: ZoneId = ZoneId.systemDefault()
    ) {
        val active = runCatching {
            kotlinx.coroutines.withTimeout(1500) { isExperimentActive().first() }
        }.getOrDefault(false)

        if (!active) {
            scheduler.cancel()
            return
        }

        val sessionKey = timeProvider.sessionKey()

        val alreadySubmitted = try {
            repo.getByDate(sessionKey) != null
        } catch (e: Exception) {
            android.util.Log.w(TAG, "repo.getByDate failed; fail-open", e)
            false
        }

        if (alreadySubmitted) {
            scheduler.cancel()
            return
        }

        val now = timeProvider.now().atZone(zone)

        val next = reminderTimes
            .distinct()
            .sorted()
            .asSequence()
            .map { ZonedDateTime.of(now.toLocalDate(), it, zone) }
            .firstOrNull { it.isAfter(now) }

        scheduler.cancel()
        if (next != null) {
            scheduler.scheduleAt(next)
        }
    }

    companion object {
        private const val TAG = "SurveyReminder"
        val DEFAULT_REMINDER_TIMES = listOf(
            LocalTime.of(19, 0),
            LocalTime.of(21, 0),
            LocalTime.of(23, 0),
            LocalTime.of(23, 59)
        )
    }
}