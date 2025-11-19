package lab.p4c.nextup.core.domain.survey.usecase

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
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(
        reminderTimes: List<LocalTime> = DEFAULT_REMINDER_TIMES,
        zone: ZoneId = ZoneId.systemDefault()
    ) {
        val sessionKey = timeProvider.sessionKey()

        // 1) 이미 오늘(세션) 설문 완료했다면 → 추가 알림 예약하지 않는다
        val alreadySubmitted = try {
            repo.getByDate(sessionKey) != null
        } catch (_: Exception) {
            // Repository 에러가 발생해도 최악의 경우 중복 알림이 갈 뿐이므로 fail-safe로 false 유지
            false
        }

        if (alreadySubmitted) return

        // 2) 오늘 다음 알림 시간 계산
        val now = timeProvider.now().atZone(zone)

        val next = reminderTimes
            .asSequence()
            .map { ZonedDateTime.of(now.toLocalDate(), it, zone) }
            .firstOrNull { it.isAfter(now) }

        // 3) 해당 시간이 아직 남아 있으면 예약
        if (next != null) {
            try {
                scheduler.scheduleAt(next)
            } catch (_: Exception) {
                // fail-safe: 스케줄 실패 시에도 앱은 동작 유지
            }
        }
        // if next == null → 오늘 첫 19:00은 지나고, 마지막 23:59도 지난 시점
        // 다음날 아침 자동 reset 이전에는 알림을 더 보낼 필요 없음
    }

    companion object {
        val DEFAULT_REMINDER_TIMES = listOf(
            LocalTime.of(19, 0),
            LocalTime.of(21, 0),
            LocalTime.of(23, 0),
            LocalTime.of(23, 59)
        )
    }
}
