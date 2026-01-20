package lab.p4c.nextup.feature.usage.infra.persist

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZoneId
import java.time.ZonedDateTime

object UsageDailyPersistScheduler {

    private const val REQ_CODE = 9203
    private const val ACTION_PERSIST_USAGE = "lab.p4c.nextup.action.PERSIST_USAGE_DAILY"

    fun scheduleNext3AM(context: Context) {
        val appCtx = context.applicationContext
        val am = appCtx.getSystemService(AlarmManager::class.java)
        val triggerAtMillis = next3AMMillis()

        val pi = PendingIntent.getBroadcast(
            appCtx,
            REQ_CODE,
            Intent(appCtx, UsageDailyPersistReceiver::class.java).setAction(ACTION_PERSIST_USAGE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            // 31 이하는 허용되지 않음
        }
    }

    fun cancel(context: Context) {
        val appCtx = context.applicationContext
        val am = appCtx.getSystemService(AlarmManager::class.java)

        val pi = PendingIntent.getBroadcast(
            appCtx,
            REQ_CODE,
            Intent(appCtx, UsageDailyPersistReceiver::class.java).setAction(ACTION_PERSIST_USAGE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    /** 테스트 함수 */
    @RequiresApi(Build.VERSION_CODES.S)
    fun scheduleInSecondsForDebug(context: Context, seconds: Long) {
        val am = context.getSystemService(AlarmManager::class.java)
        val triggerAt = System.currentTimeMillis() + seconds * 1000L

        val pi = PendingIntent.getBroadcast(
            context,
            REQ_CODE,
            Intent(context, UsageDailyPersistReceiver::class.java),
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
