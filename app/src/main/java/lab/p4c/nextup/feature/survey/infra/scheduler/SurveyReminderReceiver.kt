package lab.p4c.nextup.feature.survey.infra.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        CoroutineScope(Dispatchers.IO).launch {
            val todayKey = timeProvider.todaySurveyDateKey()
            val existing = surveyRepository.getByDate(todayKey)

            if (existing == null) {
                val ok = notifier.notifyDailySurvey()
                if (!ok) Log.w(TAG, "Notifications not permitted; skipped.")
            } else {
                Log.d(TAG, "Survey already completed for $todayKey. Skip notification.")
            }

            try {
                checkAndReschedule()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule reminder", e)
            }
        }
    }

    companion object {
        private const val TAG = "SurveyReminderReceiver"
    }
}
