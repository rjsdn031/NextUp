package lab.p4c.nextup.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import lab.p4c.nextup.feature.usage.infra.persist.UsageDailyPersistScheduler

@HiltAndroidApp
class NextUpApp : Application() {

    override fun onCreate() {
        super.onCreate()
        UsageDailyPersistScheduler.scheduleNext3AM(applicationContext)
    }
}