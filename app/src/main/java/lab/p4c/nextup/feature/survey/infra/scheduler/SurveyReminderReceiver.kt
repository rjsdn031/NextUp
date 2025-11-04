package lab.p4c.nextup.feature.survey.infra.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import lab.p4c.nextup.core.domain.survey.usecase.ScheduleDailySurveyReminder
import lab.p4c.nextup.feature.survey.infra.notifier.SurveyNotifier
import javax.inject.Inject

@AndroidEntryPoint
class SurveyReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notifier: SurveyNotifier
    @Inject lateinit var scheduleDailySurveyReminder: ScheduleDailySurveyReminder

    override fun onReceive(context: Context, intent: Intent?) {
        val ok = notifier.notifyDailySurvey()
        if (!ok) Log.w(TAG, "Notifications not permitted; skipped.")

        // 정책: 매일 21:00
        scheduleDailySurveyReminder() // 기본값 21:00이 되게 오버로드 구현해둔 버전
    }

    companion object {
        private const val TAG = "SurveyReminderReceiver"
    }
}
