package lab.p4c.nextup.feature.uploader.infra.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZoneId
import java.time.ZonedDateTime

object UploadAlarmScheduler {

    const val EXTRA_END_MS = "extra_end_ms"
    private const val REQ_CODE = 9301
    private const val ACTION_UPLOAD_DAILY = "lab.p4c.nextup.action.UPLOAD_DAILY"

    fun scheduleNext3AM(context: Context) {
        val appCtx = context.applicationContext
        val am = appCtx.getSystemService(AlarmManager::class.java)

        val triggerAtMillis = next3AMMillis()

        val intent = Intent(appCtx, UploadTriggerReceiver::class.java)
            .setAction(UploadTriggerReceiver.UPLOAD_DAILY)
            .putExtra(EXTRA_END_MS, triggerAtMillis)

        val pi = PendingIntent.getBroadcast(
            appCtx,
            REQ_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pi
        )
    }

    fun cancel(context: Context) {
        val appCtx = context.applicationContext
        val am = appCtx.getSystemService(AlarmManager::class.java)

        val intent = Intent(appCtx, UploadTriggerReceiver::class.java)
            .setAction(ACTION_UPLOAD_DAILY)

        val pi = PendingIntent.getBroadcast(
            appCtx,
            REQ_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    /** 디버그 테스트용 */
    @RequiresApi(Build.VERSION_CODES.S)
    fun scheduleInSecondsForDebug(context: Context, seconds: Long) {
        val appCtx = context.applicationContext
        val am = appCtx.getSystemService(AlarmManager::class.java)
        val triggerAt = System.currentTimeMillis() + seconds * 1000L

        val intent = Intent(appCtx, UploadTriggerReceiver::class.java)
            .setAction(ACTION_UPLOAD_DAILY)
            .putExtra(EXTRA_END_MS, triggerAt)

        val pi = PendingIntent.getBroadcast(
            appCtx,
            REQ_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            return
        }
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    private fun next3AMMillis(zone: ZoneId = ZoneId.systemDefault()): Long {
        val now = ZonedDateTime.now(zone)
        val next = if (now.hour < 3) {
            now.withHour(3).withMinute(0).withSecond(0).withNano(0)
        } else {
            now.plusDays(1).withHour(3).withMinute(0).withSecond(0).withNano(0)
        }
        return next.toInstant().toEpochMilli()
    }
}
