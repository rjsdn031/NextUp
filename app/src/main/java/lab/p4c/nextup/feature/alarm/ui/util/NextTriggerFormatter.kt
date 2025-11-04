package lab.p4c.nextup.feature.alarm.ui.util

import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object NextTriggerFormatter {

    fun formatKor(
        triggerAtUtcMillis: Long,
        now: ZonedDateTime,
        locale: Locale = Locale.KOREA
    ): String {
        val zone = now.zone
        val trigger = Instant.ofEpochMilli(triggerAtUtcMillis).atZone(zone)

        val dayDiff = Duration.between(
            now.toLocalDate().atStartOfDay(zone),
            trigger.toLocalDate().atStartOfDay(zone)
        ).toDays()

        val datePart = when (dayDiff) {
            0L -> "오늘"
            1L -> "내일"
            else -> trigger.format(DateTimeFormatter.ofPattern("M월 d일 (E)", locale))
        }

        val timePart = "%02d:%02d".format(trigger.hour, trigger.minute)

        val diff = Duration.between(now, trigger)
        val hours = diff.toHours()
        val minutes = diff.minusHours(hours).toMinutes()
        val remain = buildString {
            if (hours > 0) append("${hours}시간 ")
            append("${minutes}분 뒤")
        }

        return "$datePart $timePart ($remain)"
    }

    fun formatKor(
        triggerAtUtcMillis: Long,
        now: ZonedDateTime,
        main: Boolean = false,
        locale: Locale = Locale.KOREA
    ): String {
        if (!main) return formatKor(triggerAtUtcMillis, now)

        val zone = now.zone
        val trigger = Instant.ofEpochMilli(triggerAtUtcMillis).atZone(zone)

        val dayDiff = Duration.between(
            now.toLocalDate().atStartOfDay(zone),
            trigger.toLocalDate().atStartOfDay(zone)
        ).toDays()

        val datePart = when (dayDiff) {
            0L -> ""    // 오늘이면 datePart Skip
            1L -> "내일"
            else -> "${dayDiff}일 후"
        }

        val diff = Duration.between(now, trigger)
        val hours = diff.toHours()
        val minutes = diff.minusHours(hours).toMinutes()
        val remain = if (hours == 0L && minutes == 0L) "잠시 후" else buildString {
            if (hours > 0) append("${hours}시간 ")
            append("${minutes}분 후")
        }

        val text = if (datePart != "") datePart else remain
        return "$text\n알람이 울립니다"
    }

    fun format(
        triggerAtUtcMillis: Long,
        now: ZonedDateTime,
        locale: Locale = Locale.KOREA
    ): String {
        // TODO: Formatting in English
        return ""
    }
}