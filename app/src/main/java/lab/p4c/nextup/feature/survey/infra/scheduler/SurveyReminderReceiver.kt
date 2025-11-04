package lab.p4c.nextup.feature.survey.infra.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import lab.p4c.nextup.feature.survey.infra.notifier.SurveyNotifier

class SurveyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val notified = SurveyNotifier(context).notifyDailySurvey()
        if (!notified) {
            Log.w(TAG, "Notifications not permitted; survey notification skipped.")
        }

        val last = intent?.getLongExtra(
            AndroidSurveyReminderScheduler.EXTRA_TRIGGER_AT, 0L
        ) ?: 0L

        AndroidSurveyReminderScheduler(context).scheduleNextDaySameTime(last)
    }

    companion object {
        private const val TAG = "SurveyReminderReceiver"
    }
}
