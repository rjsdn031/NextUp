package lab.p4c.nextup.core.domain.system

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 하루를 '03:00' 기준으로 나누는 세션 키.
 * 예: 2025-11-19 01:30 → sessionKey = "2025-11-18"
 */
fun TimeProvider.sessionKey(
    rolloverHour: Int = 3,
    zone: ZoneId = ZoneId.systemDefault()
): String {
    val now = nowLocal(zone)
    val date: LocalDate =
        if (now.hour < rolloverHour)
            now.toLocalDate().minusDays(1)
        else
            now.toLocalDate()

    return date.toString() // ex. "2025-11-18"
}

/**
 * UTC(ms) 기준으로 DataKey를 산출한다, SessionKey와 동일
 * 별도 Instant를 사용한다
 */
fun dateKeyFromUtcEpochMillis(
    timestampMsUtc: Long,
    rolloverHour: Int = 3,
    zone: ZoneId = ZoneId.systemDefault()
): String {
    val localDateTime = Instant.ofEpochMilli(timestampMsUtc)
        .atZone(zone)
        .toLocalDateTime()

    val date: LocalDate =
        if (localDateTime.hour < rolloverHour)
            localDateTime.toLocalDate().minusDays(1)
        else
            localDateTime.toLocalDate()

    return date.toString()
}

fun TimeProvider.todaySurveyDateKey(): String = sessionKey()

fun TimeProvider.yesterdaySurveyDateKey(): String {
    val today = LocalDate.parse(sessionKey())
    return today.minusDays(1).toString()
}