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

    @Inject lateinit var uploadRunner: UploadRunner

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("UsagePersist", "onReceive action=${intent.action}")
        Log.d("UsagePersist", "extras=${intent.extras?.keySet()?.joinToString()}")
        val action = intent.action
        if (action != ACTION_RUN_UPLOADS) {
            Log.d(TAG, "Ignore: action=$action")
            return
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                Log.d(TAG, "Trigger received -> drain()")
                uploadRunner.drain(maxItems = 50)
                Log.d(TAG, "drain() finished")
            } catch (t: Throwable) {
                Log.e(TAG, "drain failed", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_RUN_UPLOADS = "lab.p4c.nextup.action.RUN_UPLOADS"
        private const val TAG = "UploadTrigger"
    }
}
