package lab.p4c.nextup.feature.alarm.infra.scheduler

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
import lab.p4c.nextup.core.domain.alarm.usecase.RescheduleAllEnabledAlarms

@AndroidEntryPoint
class AlarmBootReceiver : BroadcastReceiver() {

    @Inject lateinit var rescheduleAllEnabledAlarms: RescheduleAllEnabledAlarms

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        if (action !in setOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_LOCKED_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED
            )
        ) return

        Log.d(TAG, "action=$action -> reschedule enabled alarms")

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                rescheduleAllEnabledAlarms()
            } catch (t: Throwable) {
                Log.e(TAG, "reschedule failed", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "AlarmBoot"
    }
}
