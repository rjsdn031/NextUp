package lab.p4c.nextup.feature.usage.infra.persist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appCtx = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("UsagePersist", "Receiver fired")

                if (!usageStatsService.hasPermission()) {
                    Log.d("UsagePersist", "No permission")
                    return@launch
                }

                val result = usageStatsService.fetch(range = Duration.ofHours(24))
                Log.d(
                    "UsagePersist",
                    "fetch error=${result.error}, apps=${result.summary.size}, sessionsApps=${result.sessionsByApp.size}"
                )

                if (result.error != null) return@launch

                val inputs = result.sessionsByApp.values.flatten().map { s ->
                    UsageSessionInput(s.packageName, s.startMillis, s.endMillis)
                }
                Log.d("UsagePersist", "inputs=${inputs.size}")

                usageRepository.saveSessions(inputs)
                Log.d("UsagePersist", "Saved to Room")

                // TODO: 1) Room에서 targetDateKey 데이터 -> NDJSON(.gz) 생성
                // TODO: 2) Firebase 업로드 성공 시 usageRepository.deleteByDateKey(targetDateKey)
            } finally {
                UsageDailyPersistScheduler.scheduleNext3AM(appCtx)
                pendingResult.finish()
            }
        }
    }
}
