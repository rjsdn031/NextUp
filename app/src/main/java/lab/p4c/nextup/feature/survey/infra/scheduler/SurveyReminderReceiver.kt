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
import lab.p4c.nextup.core.domain.survey.usecase.CheckAndRescheduleSurveyReminder
import lab.p4c.nextup.feature.survey.infra.notifier.SurveyNotifier

@AndroidEntryPoint
class SurveyReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var notifier: SurveyNotifier
    @Inject lateinit var checkAndReschedule: CheckAndRescheduleSurveyReminder

    override fun onReceive(context: Context, intent: Intent?) {
        val ok = notifier.notifyDailySurvey()
        if (!ok) Log.w(TAG, "Notifications not permitted; skipped.")

        CoroutineScope(Dispatchers.IO).launch {
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
