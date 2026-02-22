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
import lab.p4c.nextup.core.domain.survey.port.SurveyRepository
import lab.p4c.nextup.core.domain.survey.usecase.ScheduleDailySurveyReminder
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.system.todaySurveyDateKey

@AndroidEntryPoint
class SurveyBootReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduleDailySurveyReminder: ScheduleDailySurveyReminder
    @Inject lateinit var surveyRepository: SurveyRepository
    @Inject lateinit var timeProvider: TimeProvider

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        Log.d(TAG, "boot event action=$action -> check survey reminder")

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val todayKey = timeProvider.todaySurveyDateKey()
                val existing = surveyRepository.getByDate(todayKey)

                if (existing == null) {
                    Log.d(TAG, "Survey not completed. Scheduling reminder.")
                    scheduleDailySurveyReminder(21, 0)
                } else {
                    Log.d(TAG, "Survey already completed. Skip scheduling.")
                }
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
