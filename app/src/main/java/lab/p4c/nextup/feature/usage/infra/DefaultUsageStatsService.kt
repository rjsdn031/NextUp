package lab.p4c.nextup.feature.usage.infra

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultUsageStatsService @Inject constructor(
    @ApplicationContext private val context: Context
) : UsageStatsService {

    override fun hasPermission(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        Log.d("UsagePerm", "mode=$mode (allowed=${mode == AppOpsManager.MODE_ALLOWED}) uid=${Process.myUid()} pkg=${context.packageName}")
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun requestPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override suspend fun fetch(range: Duration): UsageStatsService.Result = withContext(Dispatchers.IO) {
        if (!hasPermission()) {
            return@withContext UsageStatsService.Result(emptyList(), emptyMap(), "권한 필요")
        }
        Log.d("Usage", "fetch called. permission=${hasPermission()}")

        val usm = context.getSystemService(UsageStatsManager::class.java)
        val endMs = System.currentTimeMillis()
        val startMs = endMs - range.toMillis()

        val sessionsByApp = linkedMapOf<String, MutableList<UsageStatsService.UsageSession>>()
        val lastForeground = hashMapOf<String, Long>()

        try {
            val events = usm.queryEvents(startMs, endMs)
                ?: return@withContext UsageStatsService.Result(emptyList(), emptyMap(), "UsageEvents is null")

            val e = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(e)
                val pkg = e.packageName ?: continue

                val isResume =
                    (e.eventType == UsageEvents.Event.ACTIVITY_RESUMED) ||
                            (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND)

                val isPause =
                    (e.eventType == UsageEvents.Event.ACTIVITY_PAUSED) ||
                            (e.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) ||
                            (e.eventType == UsageEvents.Event.ACTIVITY_STOPPED)

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
                                    UsageStatsService.UsageSession(
                                        startMillis = start,
                                        endMillis = e.timeStamp,
                                        packageName = pkg
                                    )
                                )
                        }
                    }

                    else -> Unit
                }
            }

            if (lastForeground.isNotEmpty()) {
                lastForeground.forEach { (pkg, start) ->
                    sessionsByApp
                        .getOrPut(pkg) { mutableListOf() }
                        .add(
                            UsageStatsService.UsageSession(
                                startMillis = start,
                                endMillis = endMs,
                                packageName = pkg
                            )
                        )
                }
            }

            val summary = sessionsByApp.map { (pkg, list) ->
                UsageStatsService.AppUsageRow(
                    packageName = pkg,
                    total = Duration.ofMillis(list.sumOf { it.durationMillis })
                )
            }.sortedByDescending { it.total }

            UsageStatsService.Result(
                summary = summary,
                sessionsByApp = sessionsByApp.mapValues { it.value.toList() },
                error = null
            )
        } catch (t: Throwable) {
            UsageStatsService.Result(emptyList(), emptyMap(), t.message ?: "UsageStats fetch failed")
        }
    }


    override fun isUsageDataAvailable(range: Duration): Boolean {
        debugDumpUsage(Duration.ofDays(30))
        Log.d("Usage", "DataCheck1")
        if (!hasPermission()) return false
        Log.d("Usage", "DataCheck2")
        val usm = context.getSystemService(UsageStatsManager::class.java)
        val endMs = System.currentTimeMillis()
        val startMs = endMs - range.toMillis()

        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startMs,
            endMs
        )
        if (!stats.isNullOrEmpty()) return true
        Log.d("Usage", "DataCheck3")

        val events = usm.queryEvents(startMs, endMs)
        val hasEvents = events != null && events.hasNextEvent()
        Log.d("Usage", "DataCheck4=${events != null}")
        return hasEvents
    }

    fun debugDumpUsage(range: Duration) {
        val usm = context.getSystemService(UsageStatsManager::class.java)
        val endMs = System.currentTimeMillis()
        val startMs = endMs - range.toMillis()

        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMs, endMs)
        Log.d("UsageDbg", "range=$range statsSize=${stats?.size ?: -1}")

        val events = usm.queryEvents(startMs, endMs)
        if (events == null) {
            Log.d("UsageDbg", "range=$range events=null")
            return
        }

        val e = UsageEvents.Event()
        var total = 0
        val typeCount = hashMapOf<Int, Int>()

        while (events.hasNextEvent() && total < 5000) {
            events.getNextEvent(e)
            total++
            typeCount[e.eventType] = (typeCount[e.eventType] ?: 0) + 1
        }

        Log.d("UsageDbg", "range=$range totalEvents=$total typeCount=$typeCount")

        val agg = usm.queryAndAggregateUsageStats(startMs, endMs)
        Log.d("UsageDbg", "aggSize=${agg?.size ?: -1}, top=${agg.entries.take(3)}")
    }
}
