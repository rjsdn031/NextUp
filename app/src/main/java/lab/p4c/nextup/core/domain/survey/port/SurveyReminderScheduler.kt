package lab.p4c.nextup.core.domain.survey.port

import java.time.ZonedDateTime

interface SurveyReminderScheduler {
    /** 주어진 시각(로컬 타임존)의 다음 실행 시점으로 예약. */
    fun scheduleAt(zdt: ZonedDateTime)

    /** 기존 예약을 모두 취소. */
    fun cancel()
}