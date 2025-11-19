package lab.p4c.nextup.feature.survey.infra.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import lab.p4c.nextup.core.domain.survey.port.SurveyReminderScheduler
import lab.p4c.nextup.platform.permission.ExactAlarmPermission
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidSurveyReminderScheduler @Inject constructor(
    @param: ApplicationContext private val context: Context,
) : SurveyReminderScheduler {

    private val alarmMgr: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun scheduleAt(zdt: ZonedDateTime) {
        if (!ExactAlarmPermission.canSchedule(context)) {
            // 필요 시 권한 요청
            Log.w(TAG, "Exact alarm not allowed. Consider directing user to Settings.")
            return
        }

        val triggerAtMillis = zdt.toInstant().toEpochMilli()
        try {
            alarmMgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                firePendingIntent() // extras 없어도 동일 identity
            )
            Log.d(TAG, "Scheduling survey reminder at $zdt (${triggerAtMillis})")
        } catch (se: SecurityException) {
            Log.w(TAG, "Exact alarm permission not granted.", se)
        }
    }

    override fun cancel() {
        alarmMgr.cancel(firePendingIntent())
        Log.d(TAG, "Cancelling all scheduled survey reminders.")
    }

    private fun firePendingIntent(): PendingIntent {
        val intent = Intent(context, SurveyReminderReceiver::class.java)
            .setAction(ACTION_SURVEY_REMINDER)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "SurveyScheduler"
        private const val REQUEST_CODE = 9121
        private const val ACTION_SURVEY_REMINDER = "lab.p4c.nextup.action.SURVEY_REMINDER"
    }
}
