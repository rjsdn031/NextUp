package lab.p4c.nextup.domain.usecase

import java.time.LocalDate

interface HolidayProvider {
    fun isHoliday(date: LocalDate): Boolean
}

/** 기본 구현: 휴일 없음으로 가정 */
class NoopHolidayProvider : HolidayProvider {
    override fun isHoliday(date: LocalDate): Boolean = false
}