package lab.p4c.nextup.feature.usage.infra

import java.time.Duration

interface UsageStatsService {

    data class UsageSession(
        val startMillis: Long,
        val endMillis: Long,
        val packageName: String
    ) {
        val durationMillis: Long get() = (endMillis - startMillis).coerceAtLeast(0L)
    }

    data class AppUsageRow(
        val packageName: String,
        val total: Duration
    )

    data class Result(
        val summary: List<AppUsageRow>,
        val sessionsByApp: Map<String, List<UsageSession>>,
        val error: String? = null
    )

    fun hasPermission(): Boolean
    fun requestPermission()
    suspend fun fetch(range: Duration = Duration.ofHours(24)): Result
    suspend fun fetchWindow(startMs: Long, endMs: Long): Result
}
