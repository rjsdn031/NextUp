package lab.p4c.nextup.feature.settings.infra

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import lab.p4c.nextup.feature.uploader.infra.scheduler.UploadAlarmScheduler

class AndroidDebugUploadTrigger @Inject constructor(
    @ApplicationContext private val context: Context,
) : DebugUploadTrigger {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun triggerInSeconds(seconds: Long) {
        UploadAlarmScheduler.scheduleInSecondsForDebug(context, seconds)
    }
}