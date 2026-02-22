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
import lab.p4c.nextup.core.domain.survey.usecase.CheckAndRescheduleSurveyReminder
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.system.todaySurveyDateKey
import lab.p4c.nextup.feature.survey.infra.notifier.SurveyNotifier

@AndroidEntryPoint
class SurveyReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var notifier: SurveyNotifier
    @Inject lateinit var checkAndReschedule: CheckAndRescheduleSurveyReminder
    @Inject lateinit var surveyRepository: SurveyRepository
    @Inject lateinit var timeProvider: TimeProvider

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val todayKey = timeProvider.todaySurveyDateKey()
                val existing = runCatching { surveyRepository.getByDate(todayKey) }.getOrNull()

                if (existing == null) {
                    val ok = notifier.notifyDailySurvey()
                    if (!ok) Log.w(TAG, "Notifications not permitted; skipped.")
                } else {
                    Log.d(TAG, "Survey already completed for $todayKey. Skip notification.")
                }

                checkAndReschedule()
            } catch (t: Throwable) {
                Log.e(TAG, "onReceive failed", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SurveyReminderReceiver"
    }
}