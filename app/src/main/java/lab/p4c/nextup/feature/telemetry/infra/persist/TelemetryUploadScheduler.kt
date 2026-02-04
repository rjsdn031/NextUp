package lab.p4c.nextup.feature.telemetry.infra.persist

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZoneId
import java.time.ZonedDateTime

object TelemetryDailyEnqueueScheduler {

    const val EXTRA_END_MS = "extra_end_ms"
    private const val REQ_CODE = 9401
    const val ACTION_ENQUEUE_TELEMETRY = "lab.p4c.nextup.action.ENQUEUE_TELEMETRY_DAILY"

    fun scheduleNext3AM(context: Context) {
        val appCtx = context.applicationContext
        val am = appCtx.getSystemService(AlarmManager::class.java)

        val triggerAtMillis = next3AMMillis()

        val intent = Intent(appCtx, TelemetryDailyEnqueueReceiver::class.java)
            .setAction(ACTION_ENQUEUE_TELEMETRY)
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

        val intent = Intent(appCtx, TelemetryDailyEnqueueReceiver::class.java)
            .setAction(ACTION_ENQUEUE_TELEMETRY)

        val pi = PendingIntent.getBroadcast(
            appCtx,
            REQ_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        am.cancel(pi)
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
