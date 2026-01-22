package lab.p4c.nextup.core.domain.alarm.service

import jakarta.inject.Inject
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.policy.HolidayProvider
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.zone.ZoneRules

class NextTriggerCalculator @Inject constructor(
    private val clock: Clock,
    private val holidayChecker: HolidayProvider
) {
    /**
     * 다음 울림 시각(UTC millis)을 계산한다.
     * - days가 비어있으면 단발성 알람
     * - days가 있으면 가장 가까운 반복 요일
     * - skipHolidays면 휴일은 건너뜀
     * - "현재와 정확히 같은 시각"은 즉시 울리지 않고 다음 회차(내일/다음 요일)로 간주한다.
     */
    fun computeUtcMillis(alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now(clock)): Long {
        val zone = now.zone
        var date = now.toLocalDate()
        val targetTime = LocalTime.of(alarm.hour, alarm.minute)

        val LOOKHEAD = 60

        fun at(date: LocalDate) = atZoned(date, targetTime, zone)
        var candidate = at(date)

        if (!candidate.isAfter(now)) {
            date = date.plusDays(1)
            candidate = at(date)
        }

        if (alarm.days.isEmpty()) {
            if (alarm.skipHolidays) {
                var guard = 0
                while (holidayChecker.isHoliday(candidate.toLocalDate()) && guard++ < LOOKHEAD) {
                    date = date.plusDays(1)
                    candidate = at(date)
                }
            }
            return candidate.toInstant().toEpochMilli()
        }

        // 반복 알람
        var guard = 0
        var iter = candidate
        while (guard++ < LOOKHEAD) {
            val okDay = iter.dayOfWeek in alarm.days
            val okHoliday = !alarm.skipHolidays || !holidayChecker.isHoliday(iter.toLocalDate())
            if (okDay && okHoliday) {
                return iter.toInstant().toEpochMilli()
            }
            iter = at(iter.toLocalDate().plusDays(1))
        }

        // fallback
        return iter.toInstant().toEpochMilli()
    }

    private fun atZoned(date: LocalDate, time: LocalTime, zone: ZoneId): ZonedDateTime {
        val ldt = LocalDateTime.of(date, time).withSecond(0).withNano(0)
        val rules: ZoneRules = zone.rules
        val offsets = rules.getValidOffsets(ldt)
        return when {
            offsets.size == 1 -> ZonedDateTime.of(ldt, zone) // 정상
            offsets.isEmpty() -> { // 갭: 다음 유효 시각으로 점프
                val trans = rules.getTransition(ldt)
                ZonedDateTime.of(trans.dateTimeAfter, zone)
            }
            else -> { // 겹침: 이른 오프셋 선택(정책에 따라 later로 바꿀 수 있음)
                ZonedDateTime.ofLocal(ldt, zone, offsets.first())
            }
        }
    }
}