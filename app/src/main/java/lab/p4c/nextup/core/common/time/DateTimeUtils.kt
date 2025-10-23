package lab.p4c.nextup.core.common.time

import java.time.*

/** yyyy-MM-dd HH:mm:ss 대신 Flutter 포맷 그대로: "M월 d일 (요일) 오전/오후 h:mm" */
fun formatDateTime(zdt: ZonedDateTime): String {
    val ampm = if (zdt.hour < 12) "오전" else "오후"
    val h12 = (zdt.hour % 12).let { if (it == 0) 12 else it }
    val minute = zdt.minute.toString().padStart(2, '0')

    // Flutter: [일, 월, 화, 수, 목, 금, 토], dt.weekday % 7
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

/** Flutter getTimeUntilAlarm:
 *  - 1분 이상 남았으면 "X시간 Y분 후\n알람이 울립니다"
 *  - 그 미만이면 "잠시 후\n알람이 울립니다"
 */
fun getTimeUntilAlarm(alarmHour: Int, alarmMinute: Int, now: ZonedDateTime): String {
    val todayAlarm = now.withHour(alarmHour).withMinute(alarmMinute)
        .withSecond(0).withNano(0)
    val alarmDateTime = if (todayAlarm.isBefore(now)) todayAlarm.plusDays(1) else todayAlarm
    val diff = Duration.between(now, alarmDateTime)
    val totalMinutes = diff.toMinutes()
    return if (totalMinutes >= 1) {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        "${hours}시간 ${minutes}분 후\n알람이 울립니다"
    } else {
        "잠시 후\n알람이 울립니다"
    }
}

/** List<DayOfWeek> → "월 화 ..." */
fun daysToKorLine(days: Collection<DayOfWeek>): String =
    days.joinToString(" ") { dayOfWeekToKor(it) }
