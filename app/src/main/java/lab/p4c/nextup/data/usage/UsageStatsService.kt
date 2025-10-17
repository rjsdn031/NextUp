package lab.p4c.nextup.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration

/**
 * Usage Stats 수집/집계를 담당.
 * - hasPermission / requestPermission: 권한 유틸
 * - fetch: [range] 구간의 이벤트를 세션/총합으로 집계해서 반환
 *
 * Manifest:
 * <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" tools:ignore="ProtectedPermissions"/>
 */
object UsageStatsService {

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
        val summary: List<AppUsageRow>,                     // 총 사용시간 내림차순
        val sessionsByApp: Map<String, List<UsageSession>>, // 앱별 세션
        val error: String? = null
    )

    /** 권한 체크(추천): AppOps로 상태 확인 */
    fun hasPermission(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * [range] 동안의 FOREGROUND/BACKGROUND 이벤트를 페어링하여 세션과 총합을 만든다.
     * 남는 포그라운드(백그라운드가 안 찍힌 경우)는 [endMs] 시각까지로 보정.
     */
    suspend fun fetch(
        context: Context,
        range: Duration = Duration.ofHours(24)
    ): Result = withContext(Dispatchers.IO) {
        if (!hasPermission(context)) {
            return@withContext Result(emptyList(), emptyMap(), "권한 필요")
        }
        val usm = context.getSystemService(UsageStatsManager::class.java)
        val endMs = System.currentTimeMillis()
        val startMs = endMs - range.toMillis()

        val sessionsByApp = linkedMapOf<String, MutableList<UsageSession>>()
        val lastForeground = hashMapOf<String, Long>()

        try {
            val events = usm.queryEvents(startMs, endMs)
                ?: return@withContext Result(emptyList(), emptyMap(), "UsageEvents is null")

            val e = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(e)
                val pkg = e.packageName ?: continue

                // 필요 시 시스템/자기 앱 필터링
                // if (pkg == "com.android.systemui" || pkg == context.packageName) continue

                val isResume =
                    (e.eventType == UsageEvents.Event.ACTIVITY_RESUMED) ||
                            (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND)

                val isPause =
                    (e.eventType == UsageEvents.Event.ACTIVITY_PAUSED) ||
                            (e.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND)

                when {
                    isResume -> {
                        val prev = lastForeground[pkg]
                        if (prev == null || e.timeStamp > prev) {
                            lastForeground[pkg] = e.timeStamp
                        }
                    }
                    isPause -> {
                        val start = lastForeground.remove(pkg) ?: continue
                        if (e.timeStamp >= start) {
                            sessionsByApp
                                .getOrPut(pkg) { mutableListOf() }
                                .add(
                                    UsageSession(
                                        startMillis = start,
                                        endMillis = e.timeStamp,
                                        packageName = pkg
                                    )
                                )
                        }
                    }
                    else -> Unit // 다른 이벤트는 무시
                }
            }

            // 닫히지 않은 포그라운드 세션 보정
            if (lastForeground.isNotEmpty()) {
                lastForeground.forEach { (pkg, start) ->
                    sessionsByApp
                        .getOrPut(pkg) { mutableListOf() }
                        .add(
                            UsageSession(
                                startMillis = start,
                                endMillis = endMs,
                                packageName = pkg
                            )
                        )
                }
            }

            val summary = sessionsByApp.map { (pkg, list) ->
                AppUsageRow(
                    packageName = pkg,
                    total = Duration.ofMillis(list.sumOf { it.durationMillis })
                )
            }.sortedByDescending { it.total }

            Result(
                summary = summary,
                sessionsByApp = sessionsByApp.mapValues { it.value.toList() },
                error = null
            )
        } catch (t: Throwable) {
            Result(emptyList(), emptyMap(), t.message ?: "UsageStats fetch failed")
        }
    }
}
