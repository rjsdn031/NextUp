package lab.p4c.nextup.core.common.time

import java.time.DayOfWeek

fun Set<DayOfWeek>.toMask(): Int =
    fold(0) { acc, d -> acc or (1 shl d.ordinal) }

private val Int.weeks: MutableSet<DayOfWeek>
    get() = DayOfWeek.entries.filterTo(mutableSetOf()) { bit -> (this and (1 shl bit.ordinal)) != 0 }

fun Int.toDayOfWeekSet(): Set<DayOfWeek> =
    DayOfWeek.entries.filterTo(mutableSetOf()) { bit -> (this and (1 shl bit.ordinal)) != 0 }

fun flutterIndicesToDays(indices: List<Int>): Set<DayOfWeek> =
    indices.map { DayOfWeek.entries[it] }.toSet()

// Set<DayOfWeek> → Flutter JSON 인덱스(정렬)
fun Set<DayOfWeek>.toFlutterIndices(): List<Int> =
    map { it.ordinal }.sorted()
