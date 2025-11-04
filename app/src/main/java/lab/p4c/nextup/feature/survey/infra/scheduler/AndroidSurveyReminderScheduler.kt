package lab.p4c.nextup.feature.survey.infra.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import lab.p4c.nextup.core.domain.survey.port.SurveyReminderScheduler
import lab.p4c.nextup.platform.permission.ExactAlarmPermission
import java.time.ZoneId
import java.time.ZonedDateTime

class AndroidSurveyReminderScheduler(
    private val context: Context
) : SurveyReminderScheduler {

    private val alarmMgr: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun scheduleAt(zdt: ZonedDateTime) {
        val triggerAtMillis = zdt.toInstant().toEpochMilli()

        if (ExactAlarmPermission.canSchedule(context)) {
            // 필요 시 권한 요청
            Log.w(TAG, "Exact alarm not allowed. Consider directing user to Settings.")
            return
        }

        try {
            alarmMgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent(triggerAtMillis)
            )
        } catch (se: SecurityException) {
            Log.w(TAG, "Exact alarm permission not granted.", se)
        }
    }

    override fun cancel() {
        alarmMgr.cancel(pendingIntent(/* extras는 identity에 영향 없음 */0L))
    }

    /**
     * 리시버에서 전달받은 마지막 트리거 시각을 기준으로 다음날 같은 시각에 재예약.
     * lastTriggerAtMillis가 0이면 지금 시각 기준 +1일 21:00처럼 고정 로직을 별도로 적용해도 된다.
     */
    fun scheduleNextDaySameTime(lastTriggerAtMillis: Long) {
        val next = if (lastTriggerAtMillis > 0L) {
            lastTriggerAtMillis + AlarmManager.INTERVAL_DAY
        } else {
            // fallback: 시스템 현지 시간 기준으로 +1일 같은 시각
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            now.plusDays(1).toInstant().toEpochMilli()
        }

        try {
            alarmMgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next,
                pendingIntent(next)
            )
        } catch (se: SecurityException) {
            Log.w(TAG, "Exact alarm permission not granted for reschedule.", se)
        }
    }

    private fun pendingIntent(triggerAtMillis: Long): PendingIntent {
        val intent = Intent(context, SurveyReminderReceiver::class.java).apply {
            putExtra(EXTRA_TRIGGER_AT, triggerAtMillis)
        }
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
        const val EXTRA_TRIGGER_AT = "extra_trigger_at"
    }
}
