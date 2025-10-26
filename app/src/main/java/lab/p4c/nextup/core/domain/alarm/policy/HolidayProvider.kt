package lab.p4c.nextup.core.domain.alarm.policy

import jakarta.inject.Inject
import java.time.LocalDate

interface HolidayProvider {
    fun isHoliday(date: LocalDate): Boolean
}

/** 기본 구현: 휴일 없음으로 가정 */
class NoopHolidayProvider @Inject constructor() : HolidayProvider {
    override fun isHoliday(date: LocalDate) = false
}