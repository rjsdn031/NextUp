package lab.p4c.nextup.feature.uploader.infra.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lab.p4c.nextup.feature.uploader.infra.runner.UploadRunner

@AndroidEntryPoint
class UploadTriggerReceiver : BroadcastReceiver() {

    @Inject lateinit var runner: UploadRunner

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val appCtx = context.applicationContext

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val endMs = intent.getLongExtra(UploadAlarmScheduler.EXTRA_END_MS, -1L)
                Log.d("UploadTrigger", "Receiver fired endMs=$endMs action=${intent.action}")

                runner.drain(maxItems = 50)
            } catch (t: Throwable) {
                Log.e("UploadTrigger", "drain failed", t)
            } finally {
                UploadAlarmScheduler.scheduleNext3AM(appCtx)
                pending.finish()
            }
        }
    }
}
