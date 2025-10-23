package lab.p4c.nextup.core.common.time

import java.time.DayOfWeek

/** Set<DayOfWeek> → bitmask */
fun Set<DayOfWeek>.toMask(): Int =
    fold(0) { acc, d -> acc or (1 shl d.ordinal) }

/** bitmask → Set<DayOfWeek> */
fun Int.toDayOfWeekSet(): Set<DayOfWeek> =
    DayOfWeek.entries.filterTo(mutableSetOf()) { bit -> (this and (1 shl bit.ordinal)) != 0 }

/** List<Int> (1=Mon..7=Sun) → Set<DayOfWeek> */
fun List<Int>.indicesToDays(): Set<DayOfWeek> =
    mapNotNull {
        when (it) {
            1 -> DayOfWeek.MONDAY
            2 -> DayOfWeek.TUESDAY
            3 -> DayOfWeek.WEDNESDAY
            4 -> DayOfWeek.THURSDAY
            5 -> DayOfWeek.FRIDAY
            6 -> DayOfWeek.SATURDAY
            7 -> DayOfWeek.SUNDAY
            else -> null
        }
    }.toSet()

/** Set<DayOfWeek> → List<Int> (1=Mon..7=Sun) */
fun Set<DayOfWeek>.daysToIndices(): List<Int> =
    map {
        when (it) {
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
            DayOfWeek.SUNDAY -> 7
        }
    }.sorted()