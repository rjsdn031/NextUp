package lab.p4c.nextup.core.domain.system

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
