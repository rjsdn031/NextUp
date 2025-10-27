package lab.p4c.nextup.core.common.time

import java.time.*

/** yyyy-MM-dd HH:mm:ss 대신 Flutter 포맷 그대로: "M월 d일 (요일) 오전/오후 h:mm" */
fun formatDateTime(zdt: ZonedDateTime): String {
    val ampm = if (zdt.hour < 12) "오전" else "오후"
    val h12 = (zdt.hour % 12).let { if (it == 0) 12 else it }
    val minute = zdt.minute.toString().padStart(2, '0')

    //[일, 월, 화, 수, 목, 금, 토], dt.weekday % 7
    val weekdayKor = when (zdt.dayOfWeek) {
        DayOfWeek.SUNDAY -> "일"
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
    }
    return "${zdt.monthValue}월 ${zdt.dayOfMonth}일 ($weekdayKor) $ampm $h12:$minute"
}

/** "오전/오후 h:mm" */
fun formatTimeOfDay(hour: Int, minute: Int): String {
    val ampm = if (hour < 12) "오전" else "오후"
    val h12 = (hour % 12).let { if (it == 0) 12 else it }
    val m = minute.toString().padStart(2, '0')
    return "$ampm $h12:$m"
}

/** java.time.DayOfWeek → "월", "화", ... */
fun dayOfWeekToKor(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "월"
    DayOfWeek.TUESDAY -> "화"
    DayOfWeek.WEDNESDAY -> "수"
    DayOfWeek.THURSDAY -> "목"
    DayOfWeek.FRIDAY -> "금"
    DayOfWeek.SATURDAY -> "토"
    DayOfWeek.SUNDAY -> "일"
}

/** List<DayOfWeek> → "월 화 ..." */
fun daysToKorLine(days: Collection<DayOfWeek>): String =
    days.joinToString(" ") { dayOfWeekToKor(it) }
