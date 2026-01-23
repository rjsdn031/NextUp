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
import lab.p4c.nextup.core.domain.auth.port.AuthClient
import lab.p4c.nextup.feature.uploader.infra.runner.UploadRunner

@AndroidEntryPoint
class UploadTriggerReceiver : BroadcastReceiver() {

    @Inject lateinit var uploadRunner: UploadRunner
    @Inject lateinit var authClient: AuthClient

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive action=${intent.action}")
        Log.d(TAG, "extras=${intent.extras?.keySet()?.joinToString()}")
        val action = intent.action
        if (action != UPLOAD_DAILY) {
            Log.d(TAG, "Ignore: action=$action")
            return
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                authClient.ensureSignedIn()

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
        const val UPLOAD_DAILY = "lab.p4c.nextup.action.UPLOAD_DAILY"
        private const val TAG = "UploadTrigger"
    }
}
