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

/**
 * Computes the next trigger instant for an [Alarm].
 *
 * Time contract:
 * - Input [now] is a zoned date-time representing the user's current zone.
 * - The returned value is an absolute UTC epoch millis.
 *
 * Policy:
 * - One-time alarm: [Alarm.days] is empty.
 * - Repeating alarm: next date whose day-of-week is included in [Alarm.days].
 * - If [Alarm.skipHolidays] is true, skip dates that are public holidays.
 * - If the candidate time is equal to or before [now], it is treated as "already passed"
 *   and the next occurrence is chosen.
 *
 * DST handling:
 * - The local target time is resolved via zone rules.
 * - Gaps (spring-forward) jump to the next valid time.
 * - Overlaps (fall-back) choose the earlier offset.
 *
 * TODO(policy):
 * - Define overlap policy explicitly (earlier vs later offset) as a domain-level decision.
 *
 * TODO(refactor):
 * - Extract search loop into a shared function to avoid duplicate holiday/day checks.
 * - Replace magic lookahead constant with a named constant at file/class level.
 */
class NextTriggerCalculator @Inject constructor(
    private val clock: Clock,
    private val holidayProvider: HolidayProvider
) {

    /**
     * Calculates the next trigger time as UTC epoch millis.
     *
     * @param alarm Alarm configuration.
     * @param now Current time in the user's zone. Defaults to [clock].
     * @return Next trigger time as UTC epoch milliseconds.
     */
    fun computeUtcMillis(
        alarm: Alarm,
        now: ZonedDateTime = ZonedDateTime.now(clock)
    ): Long {
        val zone = now.zone
        val targetTime = LocalTime.of(alarm.hour, alarm.minute)

        // TODO(refactor): Move to companion/file-level constant.
        val lookAheadDays = 60

        fun at(date: LocalDate) = atZoned(date, targetTime, zone)

        var date = now.toLocalDate()
        var candidate = at(date)

        if (!candidate.isAfter(now)) {
            date = date.plusDays(1)
            candidate = at(date)
        }

        // One-time alarm
        if (alarm.days.isEmpty()) {
            if (alarm.skipHolidays) {
                var guard = 0
                while (holidayProvider.isHoliday(candidate.toLocalDate()) && guard++ < lookAheadDays) {
                    date = date.plusDays(1)
                    candidate = at(date)
                }
            }
            return candidate.toInstant().toEpochMilli()
        }

        // Repeating alarm
        var guard = 0
        var iter = candidate
        while (guard++ < lookAheadDays) {
            val okDay = iter.dayOfWeek in alarm.days
            val okHoliday = !alarm.skipHolidays || !holidayProvider.isHoliday(iter.toLocalDate())
            if (okDay && okHoliday) {
                return iter.toInstant().toEpochMilli()
            }
            iter = at(iter.toLocalDate().plusDays(1))
        }

        // Fallback: return the last computed candidate after lookahead window.
        // TODO(revisit-design): Consider throwing/returning Result if lookahead exhaustion should be surfaced.
        return iter.toInstant().toEpochMilli()
    }

    /**
     * Resolves a local date/time into a [ZonedDateTime] under [zone] rules.
     *
     * - If exactly one offset is valid, use it.
     * - If no offsets are valid (gap), jump to the transition's "after" time.
     * - If multiple offsets are valid (overlap), choose the earlier offset.
     */
    private fun atZoned(date: LocalDate, time: LocalTime, zone: ZoneId): ZonedDateTime {
        val ldt = LocalDateTime.of(date, time).withSecond(0).withNano(0)
        val rules: ZoneRules = zone.rules
        val offsets = rules.getValidOffsets(ldt)

        return when {
            offsets.size == 1 -> ZonedDateTime.of(ldt, zone)
            offsets.isEmpty() -> {
                val trans = rules.getTransition(ldt)
                ZonedDateTime.of(trans.dateTimeAfter, zone)
            }
            else -> ZonedDateTime.ofLocal(ldt, zone, offsets.first())
        }
    }
}
