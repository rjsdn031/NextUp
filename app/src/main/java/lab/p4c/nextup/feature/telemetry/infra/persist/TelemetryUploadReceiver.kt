package lab.p4c.nextup.feature.telemetry.infra.persist

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
import java.time.Duration
import lab.p4c.nextup.core.domain.system.dateKeyFromUtcEpochMillis
import lab.p4c.nextup.core.domain.upload.UploadType
import lab.p4c.nextup.feature.uploader.data.repository.UploadQueueRepository
import lab.p4c.nextup.feature.uploader.infra.scheduler.UploadTriggerReceiver

@AndroidEntryPoint
class TelemetryDailyEnqueueReceiver : BroadcastReceiver() {

    @Inject lateinit var uploadQueueRepository: UploadQueueRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != TelemetryDailyEnqueueScheduler.ACTION_ENQUEUE_TELEMETRY) {
            Log.d("TelemetryEnqueue", "Ignore: action=$action")
            return
        }

        val pendingResult = goAsync()
        val appCtx = context.applicationContext
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val endMs = intent.getLongExtra(TelemetryDailyEnqueueScheduler.EXTRA_END_MS, -1L)
                    .takeIf { it > 0L } ?: System.currentTimeMillis()

                val targetDateKey = dateKeyFromUtcEpochMillis(
                    endMs - Duration.ofHours(3).toMillis()
                )

                uploadQueueRepository.enqueue(
                    type = UploadType.TELEMETRY,
                    dateKey = targetDateKey,
                    localRef = null,
                    runAtMs = System.currentTimeMillis(),
                    priority = 10
                )

                Log.d("TelemetryEnqueue", "Enqueued upload: dateKey=$targetDateKey")

                appCtx.sendBroadcast(
                    Intent(appCtx, UploadTriggerReceiver::class.java)
                        .setAction(UploadTriggerReceiver.UPLOAD_DAILY)
                )
            } catch (t: Throwable) {
                Log.e("TelemetryEnqueue", "Enqueue failed", t)
            } finally {
                TelemetryDailyEnqueueScheduler.scheduleNext3AM(appCtx)
                pendingResult.finish()
            }
        }
    }
}
