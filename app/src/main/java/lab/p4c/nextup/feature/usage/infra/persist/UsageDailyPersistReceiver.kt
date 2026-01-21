package lab.p4c.nextup.feature.usage.infra.persist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lab.p4c.nextup.core.domain.system.dateKeyFromUtcEpochMillis
import lab.p4c.nextup.feature.usage.data.repository.UsageRepository
import lab.p4c.nextup.feature.usage.data.repository.UsageSessionInput
import lab.p4c.nextup.feature.usage.infra.UsageStatsService
import java.time.Duration

@AndroidEntryPoint
class UsageDailyPersistReceiver : BroadcastReceiver() {

    @Inject lateinit var usageStatsService: UsageStatsService
    @Inject lateinit var usageRepository: UsageRepository
    @Inject lateinit var uploadQueueRepository: lab.p4c.nextup.feature.uploader.data.repository.UploadQueueRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != UsageDailyPersistScheduler.ACTION_PERSIST_USAGE) {
            Log.d("UsagePersist", "Ignore: action=$action")
            return
        }

        val pendingResult = goAsync()
        val appCtx = context.applicationContext
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                Log.d("UsagePersist", "Receiver fired")

                if (!usageStatsService.hasPermission()) {
                    Log.d("UsagePersist", "No permission")
                    return@launch
                }

                val endMs = intent.getLongExtra(UsageDailyPersistScheduler.EXTRA_END_MS, -1L)
                    .takeIf { it > 0L } ?: System.currentTimeMillis()

                val startMs = endMs - Duration.ofHours(24).toMillis()

                val result = usageStatsService.fetchWindow(startMs, endMs)

                Log.d(
                    "UsagePersist",
                    "window=[$startMs,$endMs) error=${result.error}, apps=${result.summary.size}, sessionsApps=${result.sessionsByApp.size}"
                )

                if (result.error != null) return@launch

                val inputs = result.sessionsByApp.values
                    .flatten()
                    .map { s -> UsageSessionInput(s.packageName, s.startMillis, s.endMillis) }

                usageRepository.saveSessions(inputs)
                Log.d("UsagePersist", "Saved to Room, inputs=${inputs.size}")

                // ✅ enqueue(USAGE)
                val targetDateKey = dateKeyFromUtcEpochMillis(
                    endMs - Duration.ofHours(3).toMillis()
                )
                val localRef = "$startMs,$endMs"

                uploadQueueRepository.enqueue(
                    type = lab.p4c.nextup.core.domain.upload.UploadType.USAGE,
                    dateKey = targetDateKey,
                    localRef = localRef,
                    runAtMs = System.currentTimeMillis(),
                    priority = 10
                )

                Log.d("UsagePersist", "Enqueued upload: dateKey=$targetDateKey localRef=$localRef")

            } catch (t: Throwable) {
                Log.e("UsagePersist", "Persist failed", t)
            } finally {
                UsageDailyPersistScheduler.scheduleNext3AM(appCtx)
                pendingResult.finish()
            }
        }
    }
}