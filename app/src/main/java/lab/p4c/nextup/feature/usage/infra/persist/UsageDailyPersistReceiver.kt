package lab.p4c.nextup.feature.usage.infra.persist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                if (!usageStatsService.hasPermission()) return@launch

                val result = usageStatsService.fetch(range = Duration.ofHours(24))
                if (result.error != null) return@launch

                val inputs = result.sessionsByApp.values
                    .flatten()
                    .map { s ->
                        UsageSessionInput(
                            packageName = s.packageName,
                            startMillis = s.startMillis,
                            endMillis = s.endMillis
                        )
                    }

                usageRepository.saveSessions(inputs)
            } finally {
                // 다음날 03:00 재예약 (setExact는 1회성)
                UsageDailyPersistScheduler.scheduleNext3AM(appCtx)
                pendingResult.finish()
            }
        }
    }
}
