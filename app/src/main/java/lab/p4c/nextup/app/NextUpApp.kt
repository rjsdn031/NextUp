package lab.p4c.nextup.app

import android.app.Application
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import lab.p4c.nextup.feature.telemetry.infra.persist.TelemetryDailyEnqueueScheduler
import lab.p4c.nextup.feature.usage.infra.persist.UsageDailyPersistScheduler

@HiltAndroidApp
class NextUpApp : Application() {

    override fun onCreate() {
        super.onCreate()
        UsageDailyPersistScheduler.scheduleNext3AM(applicationContext)
        TelemetryDailyEnqueueScheduler.scheduleNext3AM(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            UsageDailyPersistScheduler.scheduleInSecondsForDebug(applicationContext, 30)
        }
    }
}