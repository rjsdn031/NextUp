package lab.p4c.nextup.feature.survey.infra.scheduler

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
import lab.p4c.nextup.core.domain.survey.usecase.ScheduleDailySurveyReminder

@AndroidEntryPoint
class SurveyBootReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduleDailySurveyReminder: ScheduleDailySurveyReminder

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        Log.d(TAG, "boot event action=$action -> reschedule survey reminder")

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                scheduleDailySurveyReminder(21, 0)
            } catch (t: Throwable) {
                Log.e(TAG, "reschedule failed", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SurveyBoot"
    }
}
