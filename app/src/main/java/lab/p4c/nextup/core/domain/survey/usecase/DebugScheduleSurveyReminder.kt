package lab.p4c.nextup.core.domain.survey.usecase

import android.util.Log
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import lab.p4c.nextup.core.domain.survey.port.SurveyReminderScheduler
import lab.p4c.nextup.core.domain.system.TimeProvider

/**
 * Survey reminder 강제 예약 디버그 유즈케이스.
 *
 * 운영 정책과 무관하게 현재 시각 기준 N초 뒤에 단일 알림을 예약한다.
 */
class DebugScheduleSurveyReminder @Inject constructor(
    private val timeProvider: TimeProvider,
    private val scheduler: SurveyReminderScheduler,
) {

    fun scheduleForceInSeconds(
        offsetSeconds: Long = 10,
        zone: ZoneId = ZoneId.systemDefault(),
    ): ZonedDateTime {

        val now = timeProvider.now().atZone(zone)

        val scheduledAt = now
            .plusSeconds(offsetSeconds)
            .withNano(0)

        Log.d(TAG, "Debug schedule requested")
        Log.d(TAG, "now=$now")
        Log.d(TAG, "offsetSeconds=$offsetSeconds")
        Log.d(TAG, "scheduledAt=$scheduledAt")

        scheduler.cancel()
        Log.d(TAG, "Previous reminder cancelled")

        scheduler.scheduleAt(scheduledAt)
        Log.d(TAG, "scheduleAt() called")

        return scheduledAt
    }

    companion object {
        private const val TAG = "SurveyReminderDebug"
    }
}