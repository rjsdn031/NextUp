package lab.p4c.nextup.feature.settings.ui.util

import java.util.concurrent.TimeUnit

/**
 * 사용 시간(ms)을 설명 라벨로 변환한다.
 */
object UsageLabelFormatter {
    fun forRollingDays(usageMillis: Long, days: Int): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(usageMillis)

        if (totalSeconds <= 0L) {
            return "${days}일 동안 사용 기록 없음"
        }

        val minutes = totalSeconds / 60
        if (minutes <= 0L) {
            return "${days}일 동안 1분 미만 사용"
        }

        val hours = minutes / 60
        val remMinutes = minutes % 60

        return if (hours > 0L) {
            "${days}일 동안 ${hours}시간 ${remMinutes}분 사용"
        } else {
            "${days}일 동안 ${minutes}분 사용"
        }
    }
}