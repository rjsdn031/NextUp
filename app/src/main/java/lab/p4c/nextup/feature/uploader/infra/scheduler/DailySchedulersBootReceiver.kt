package lab.p4c.nextup.feature.uploader.infra.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import lab.p4c.nextup.feature.telemetry.infra.persist.TelemetryDailyEnqueueScheduler
import lab.p4c.nextup.feature.usage.infra.persist.UsageDailyPersistScheduler

class DailySchedulersBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val appCtx = context.applicationContext

        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        Log.d(TAG, "boot event action=$action -> reschedule daily alarms")

        UsageDailyPersistScheduler.scheduleNext3AM(appCtx)
        TelemetryDailyEnqueueScheduler.scheduleNext3AM(appCtx)
        // TODO: survey 업로드
    }

    companion object {
        private const val TAG = "DailyBoot"
    }
}
